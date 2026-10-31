package co.edu.unbosque.service.impl;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import co.edu.unbosque.entity.Reserva;
import co.edu.unbosque.repository.AlojamientoRepository;
import co.edu.unbosque.repository.PagoRepository;
import co.edu.unbosque.repository.ReservaRepository;
import co.edu.unbosque.repository.UsuarioRepository;
import co.edu.unbosque.service.api.EventoAuditoriaServiceAPI;
import co.edu.unbosque.service.api.ReporteServiceAPI;
import co.edu.unbosque.utils.Constantes;
import co.edu.unbosque.utils.GeneradorCsv;
import co.edu.unbosque.utils.GeneradorPdf;
import co.edu.unbosque.utils.ReglaNegocioException;
import co.edu.unbosque.utils.Utilidad;

@Service
public class ReporteServiceImpl implements ReporteServiceAPI {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private AlojamientoRepository alojamientoRepository;

    @Autowired
    private ReservaRepository reservaRepository;

    @Autowired
    private PagoRepository pagoRepository;

    @Autowired
    private EventoAuditoriaServiceAPI eventoAuditoriaServiceAPI;

    /** HU-46: indicadores principales para el administrador. */
    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> dashboard() {
        Map<String, Object> datos = new LinkedHashMap<>();

        Map<String, Long> usuarios = new LinkedHashMap<>();
        usuarios.put("huespedes", usuarioRepository.countByRol(Constantes.ROL_HUESPED));
        usuarios.put("anfitriones", usuarioRepository.countByRol(Constantes.ROL_ANFITRION));
        usuarios.put("restringidos", usuarioRepository.countByEstado(Constantes.USUARIO_RESTRINGIDO));
        datos.put("usuarios", usuarios);

        Map<String, Long> publicaciones = new LinkedHashMap<>();
        for (String estado : List.of(Constantes.ALOJAMIENTO_BORRADOR, Constantes.ALOJAMIENTO_PENDIENTE_REVISION,
                Constantes.ALOJAMIENTO_ACTIVA, Constantes.ALOJAMIENTO_PAUSADA, Constantes.ALOJAMIENTO_BLOQUEADA)) {
            publicaciones.put(estado, alojamientoRepository.countByEstado(estado));
        }
        datos.put("publicaciones", publicaciones);

        Map<String, Long> reservas = new LinkedHashMap<>();
        for (String estado : List.of(Constantes.RESERVA_EN_REVISION, Constantes.RESERVA_PENDIENTE_PAGO,
                Constantes.RESERVA_CONFIRMADA, Constantes.RESERVA_PAGO_FALLIDO, Constantes.RESERVA_CANCELADA,
                Constantes.RESERVA_COMPLETADA)) {
            reservas.put(estado, reservaRepository.countByEstado(estado));
        }
        datos.put("reservas", reservas);
        datos.put("ingresosAprobadosCOP", pagoRepository.sumarAprobados());
        return datos;
    }

    /** HU-47 exportar y HU-48 registrar quién exporta (auditoría en la misma transacción). */
    @Override
    @Transactional
    public byte[] exportarReservas(Long adminId, String formato, LocalDate desde, LocalDate hasta) {
        LocalDate inicio = desde == null ? LocalDate.now().minusMonths(1) : desde;
        LocalDate fin = hasta == null ? LocalDate.now() : hasta;
        if (fin.isBefore(inicio)) {
            throw new ReglaNegocioException("La fecha final no puede ser anterior a la inicial.");
        }
        String tipo = formato == null ? "csv" : formato.trim().toLowerCase();
        if (!tipo.equals("csv") && !tipo.equals("pdf")) {
            throw new ReglaNegocioException("Formato no válido. Usa csv o pdf.");
        }
        List<Reserva> lista = reservaRepository.findByCreadaEnBetweenOrderByCreadaEnAsc(
                inicio.atStartOfDay(), fin.plusDays(1).atStartOfDay());

        List<String> encabezados = List.of("id", "alojamiento", "huesped", "llegada", "salida", "noches", "total", "estado",
                "creada");
        List<List<String>> filas = new ArrayList<>();
        for (Reserva r : lista) {
            filas.add(List.of(String.valueOf(r.getId()), String.valueOf(r.getAlojamientoId()),
                    String.valueOf(r.getHuespedId()), r.getFechaLlegada().toString(), r.getFechaSalida().toString(),
                    String.valueOf(r.getNoches()), r.getTotal().toPlainString(), r.getEstado(),
                    r.getCreadaEn().toLocalDate().toString()));
        }

        eventoAuditoriaServiceAPI.registrar(adminId, "REPORTE", null, "EXPORTAR_REPORTE", Constantes.AUDITORIA_EXITO,
                Utilidad.detalle("formato", tipo, "desde", inicio.toString(), "hasta", fin.toString(),
                        "registros", lista.size()));

        if (tipo.equals("csv")) {
            return GeneradorCsv.generar(encabezados, filas);
        }
        List<String> lineas = new ArrayList<>();
        lineas.add("Periodo: " + inicio + " a " + fin + "   Total de reservas: " + lista.size());
        lineas.add("");
        lineas.add(String.format("%-6s %-6s %-6s %-11s %-11s %-4s %-14s %-15s", "ID", "ALOJ", "HUESP", "LLEGADA",
                "SALIDA", "NOCH", "TOTAL COP", "ESTADO"));
        for (Reserva r : lista) {
            lineas.add(String.format("%-6d %-6d %-6d %-11s %-11s %-4d %-14s %-15s", r.getId(), r.getAlojamientoId(),
                    r.getHuespedId(), r.getFechaLlegada(), r.getFechaSalida(), r.getNoches(),
                    r.getTotal().toPlainString(), r.getEstado()));
        }
        return GeneradorPdf.generar("Reporte de reservas - Marketplace Inversiones LR", lineas);
    }
}
