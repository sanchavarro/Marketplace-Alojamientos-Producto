package co.edu.unbosque.service.impl;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import co.edu.unbosque.dto.SaludResponse;
import co.edu.unbosque.service.api.EventoOutboxServiceAPI;
import co.edu.unbosque.service.api.SaludServiceAPI;
import co.edu.unbosque.utils.Constantes;
import co.edu.unbosque.utils.MetricasSistema;
import co.edu.unbosque.utils.PasarelaPagoAdapter;

@Service
public class SaludServiceImpl implements SaludServiceAPI {

    @Autowired
    private PasarelaPagoAdapter pasarelaPagoAdapter;

    @Autowired
    private EventoOutboxServiceAPI eventoOutboxServiceAPI;

    @Override
    public SaludResponse estado() {
        SaludResponse respuesta = new SaludResponse();
        respuesta.setEstado("ok");
        respuesta.setAplicacion("Marketplace de Alojamientos - Inversiones LR");
        respuesta.setHora(LocalDateTime.now());
        respuesta.setPasarelaPagos(pasarelaPagoAdapter.getNombrePasarela());
        respuesta.setCortacircuitos(pasarelaPagoAdapter.getEstadoCortacircuitos());
        return respuesta;
    }

    @Override
    public Map<String, Object> metricas() {
        Map<String, Object> datos = new LinkedHashMap<>(MetricasSistema.getInstancia().resumen());
        datos.put("cortacircuitosPagos", pasarelaPagoAdapter.getEstadoCortacircuitos());
        Map<String, Long> outbox = new LinkedHashMap<>();
        outbox.put("pendientes", eventoOutboxServiceAPI.contarPorEstado(Constantes.OUTBOX_PENDIENTE));
        outbox.put("procesados", eventoOutboxServiceAPI.contarPorEstado(Constantes.OUTBOX_PROCESADO));
        outbox.put("error", eventoOutboxServiceAPI.contarPorEstado(Constantes.OUTBOX_ERROR));
        datos.put("outbox", outbox);
        return datos;
    }
}
