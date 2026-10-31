package co.edu.unbosque.service.impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import co.edu.unbosque.dto.CancelacionResponse;
import co.edu.unbosque.entity.Alojamiento;
import co.edu.unbosque.entity.Cancelacion;
import co.edu.unbosque.entity.Pago;
import co.edu.unbosque.entity.PoliticaCancelacion;
import co.edu.unbosque.entity.Reserva;
import co.edu.unbosque.repository.AlojamientoRepository;
import co.edu.unbosque.repository.CancelacionRepository;
import co.edu.unbosque.repository.PagoRepository;
import co.edu.unbosque.repository.PoliticaCancelacionRepository;
import co.edu.unbosque.repository.ReservaRepository;
import co.edu.unbosque.service.api.CancelacionServiceAPI;
import co.edu.unbosque.service.api.DisponibilidadServiceAPI;
import co.edu.unbosque.service.api.EventoAuditoriaServiceAPI;
import co.edu.unbosque.service.api.EventoOutboxServiceAPI;
import co.edu.unbosque.service.api.PagoServiceAPI;
import co.edu.unbosque.utils.AccesoDenegadoException;
import co.edu.unbosque.utils.Constantes;
import co.edu.unbosque.utils.EstrategiaCancelacion;
import co.edu.unbosque.utils.FabricaEstrategiaCancelacion;
import co.edu.unbosque.utils.GenericServiceImpl;
import co.edu.unbosque.utils.MaquinaEstados;
import co.edu.unbosque.utils.ReglaNegocioException;
import co.edu.unbosque.utils.ResourceNotFoundException;
import co.edu.unbosque.utils.ResultadoPago;
import co.edu.unbosque.utils.Utilidad;

/**
 * Cancelación de reservas.
 * HU-33: la política del alojamiento (patrón Estrategia) calcula el porcentaje a devolver.
 * HU-34: se registran los datos de la cancelación. HU-35: se libera el periodo.
 * El reembolso en Stripe se hace fuera de la transacción y con clave de idempotencia propia,
 * así un doble clic no devuelve el dinero dos veces.
 */
@Service
public class CancelacionServiceImpl extends GenericServiceImpl<Cancelacion, Long> implements CancelacionServiceAPI {

    private static final List<String> CANCELABLES = List.of(Constantes.RESERVA_CONFIRMADA,
            Constantes.RESERVA_PENDIENTE_PAGO, Constantes.RESERVA_EN_REVISION);

    @Autowired
    private CancelacionRepository cancelacionRepository;

    @Autowired
    private ReservaRepository reservaRepository;

    @Autowired
    private AlojamientoRepository alojamientoRepository;

    @Autowired
    private PoliticaCancelacionRepository politicaCancelacionRepository;

    @Autowired
    private PagoRepository pagoRepository;

    @Autowired
    private PagoServiceAPI pagoServiceAPI;

    @Autowired
    private DisponibilidadServiceAPI disponibilidadServiceAPI;

    @Autowired
    private EventoAuditoriaServiceAPI eventoAuditoriaServiceAPI;

    @Autowired
    private EventoOutboxServiceAPI eventoOutboxServiceAPI;

    private TransactionTemplate transaccion;

    @Autowired
    public void setTransactionManager(PlatformTransactionManager transactionManager) {
        this.transaccion = new TransactionTemplate(transactionManager);
    }

    @Override
    public CrudRepository<Cancelacion, Long> getDao() {
        return cancelacionRepository;
    }

    @Override
    public CancelacionResponse simular(Long usuarioId, String rol, Long reservaId) {
        Reserva reserva = validarCancelable(usuarioId, rol, reservaId);
        return calcular(reserva);
    }

    @Override
    public CancelacionResponse cancelar(Long usuarioId, String rol, Long reservaId, String motivo) {
        Reserva reserva = validarCancelable(usuarioId, rol, reservaId);
        CancelacionResponse calculo = calcular(reserva);

        // 1. Reembolso fuera de la transacción (puede tardar hasta 10 s, ADR-04).
        ResultadoPago reembolso = null;
        Pago pagoAprobado = pagoServiceAPI.pagoAprobado(reservaId);
        if (pagoAprobado != null && calculo.getValorDevolucion().compareTo(BigDecimal.ZERO) > 0) {
            reembolso = pagoServiceAPI.reembolsar(pagoAprobado, calculo.getValorDevolucion(),
                    "reembolso-reserva-" + reservaId);
            if (!reembolso.isAprobado()) {
                throw new ReglaNegocioException("La pasarela no aprobó el reembolso: " + reembolso.getMensaje());
            }
        }
        final ResultadoPago reembolsoFinal = reembolso;

        // 2. Transacción: estado, pago, cancelación, periodo liberado, auditoría y outbox.
        return transaccion.execute(estado -> {
            Reserva r = reservaRepository.findById(reservaId).orElseThrow();
            MaquinaEstados.validarReserva(r.getEstado(), Constantes.RESERVA_CANCELADA);
            r.setEstado(Constantes.RESERVA_CANCELADA);
            reservaRepository.save(r);

            if (reembolsoFinal != null) {
                pagoAprobado.setEstado(Constantes.PAGO_DEVUELTO);
                pagoRepository.save(pagoAprobado);
            }
            String estadoFinal = estadoFinal(calculo);
            Cancelacion cancelacion = new Cancelacion();
            cancelacion.setReservaId(reservaId);
            cancelacion.setUsuarioId(usuarioId);
            cancelacion.setMotivo(motivo == null || motivo.isBlank() ? null : motivo.trim());
            cancelacion.setValorDevolucion(calculo.getValorDevolucion());
            cancelacion.setEstadoFinal(estadoFinal);
            cancelacion.setFecha(LocalDateTime.now());
            cancelacionRepository.save(cancelacion);

            disponibilidadServiceAPI.liberarPorReserva(reservaId);

            eventoAuditoriaServiceAPI.registrar(usuarioId, "RESERVA", reservaId, "CANCELAR_RESERVA",
                    Constantes.AUDITORIA_EXITO, Utilidad.detalle("politica", calculo.getPolitica(),
                            "porcentaje", calculo.getPorcentajeDevolucion(), "devolucion", calculo.getValorDevolucion(),
                            "referenciaReembolso", reembolsoFinal == null ? null : reembolsoFinal.getReferencia()));
            Alojamiento alojamiento = alojamientoRepository.findById(r.getAlojamientoId()).orElseThrow();
            eventoOutboxServiceAPI.publicar(Constantes.EVENTO_RESERVA_CANCELADA, r.getHuespedId(),
                    "Reserva cancelada", "Tu reserva en \"" + alojamiento.getTitulo() + "\" fue cancelada. Devolución: $"
                            + calculo.getValorDevolucion() + " COP.");
            eventoOutboxServiceAPI.publicar(Constantes.EVENTO_RESERVA_CANCELADA, alojamiento.getAnfitrionId(),
                    "Reserva cancelada", "Se canceló la reserva " + reservaId + " de \"" + alojamiento.getTitulo()
                            + "\". Las fechas quedaron libres.");

            calculo.setEstadoReserva(Constantes.RESERVA_CANCELADA);
            calculo.setEstadoFinal(estadoFinal);
            calculo.setReferenciaReembolso(reembolsoFinal == null ? null : reembolsoFinal.getReferencia());
            calculo.setFecha(cancelacion.getFecha());
            return calculo;
        });
    }

    private Reserva validarCancelable(Long usuarioId, String rol, Long reservaId) {
        Reserva reserva = reservaRepository.findById(reservaId)
                .orElseThrow(() -> new ResourceNotFoundException("Reserva no encontrada."));
        boolean esHuesped = reserva.getHuespedId().equals(usuarioId);
        if (!esHuesped && !Constantes.ROL_ADMINISTRADOR.equals(rol)) {
            throw new AccesoDenegadoException("Solo el huésped de la reserva puede cancelarla.");
        }
        if (!CANCELABLES.contains(reserva.getEstado())) {
            throw new ReglaNegocioException("La reserva no se puede cancelar en estado " + reserva.getEstado() + ".");
        }
        if (!LocalDate.now().isBefore(reserva.getFechaLlegada())) {
            throw new ReglaNegocioException("Solo se puede cancelar antes de la fecha de llegada.");
        }
        return reserva;
    }

    /** HU-33: aplica la estrategia de la política del alojamiento. */
    private CancelacionResponse calcular(Reserva reserva) {
        Alojamiento alojamiento = alojamientoRepository.findById(reserva.getAlojamientoId()).orElseThrow();
        PoliticaCancelacion politica = alojamiento.getPoliticaId() == null ? null
                : politicaCancelacionRepository.findById(alojamiento.getPoliticaId()).orElse(null);
        String tipo = politica == null ? Constantes.POLITICA_FLEXIBLE : politica.getTipo();
        EstrategiaCancelacion estrategia = FabricaEstrategiaCancelacion.obtener(tipo);

        boolean pagada = Constantes.RESERVA_CONFIRMADA.equals(reserva.getEstado());
        BigDecimal totalPagado = pagada ? reserva.getTotal() : BigDecimal.ZERO;
        int porcentaje = pagada ? estrategia.porcentajeDevolucion(LocalDate.now(), reserva.getFechaLlegada()) : 0;
        BigDecimal devolucion = pagada
                ? estrategia.calcularDevolucion(totalPagado, LocalDate.now(), reserva.getFechaLlegada())
                : BigDecimal.ZERO;

        CancelacionResponse respuesta = new CancelacionResponse();
        respuesta.setReservaId(reserva.getId());
        respuesta.setEstadoReserva(reserva.getEstado());
        respuesta.setPolitica(tipo);
        respuesta.setReglaPolitica(estrategia.descripcion());
        respuesta.setPorcentajeDevolucion(porcentaje);
        respuesta.setTotalPagado(totalPagado);
        respuesta.setValorDevolucion(devolucion);
        respuesta.setEstadoFinal(estadoFinal(respuesta));
        return respuesta;
    }

    private String estadoFinal(CancelacionResponse calculo) {
        if (calculo.getTotalPagado().compareTo(BigDecimal.ZERO) == 0) {
            return "CANCELADA_SIN_PAGO";
        }
        if (calculo.getPorcentajeDevolucion() >= 100) {
            return "CANCELADA_REEMBOLSO_TOTAL";
        }
        if (calculo.getPorcentajeDevolucion() > 0) {
            return "CANCELADA_REEMBOLSO_PARCIAL";
        }
        return "CANCELADA_SIN_REEMBOLSO";
    }
}
