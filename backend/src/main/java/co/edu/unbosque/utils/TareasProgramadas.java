package co.edu.unbosque.utils;

import java.util.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import co.edu.unbosque.service.api.ReservaServiceAPI;

/**
 * Tareas en segundo plano (ADR-08): completar estadías terminadas, vencer pagos pendientes
 * y limpiar claves de idempotencia de más de 24 h (ADR-03).
 */
@Component
public class TareasProgramadas {

    private static final Logger logger = Logger.getLogger(TareasProgramadas.class.getName());

    @Autowired
    private ReservaServiceAPI reservaServiceAPI;

    /** Todos los días a las 00:05. */
    @Scheduled(cron = "0 5 0 * * *")
    public void completarReservas() {
        int total = reservaServiceAPI.completarReservasFinalizadas();
        logger.info("tarea=completar_reservas total=" + total);
    }

    /** Cada hora. */
    @Scheduled(fixedRate = 3600000, initialDelay = 60000)
    public void mantenimiento() {
        int vencidas = reservaServiceAPI.vencerPagosPendientes();
        int claves = reservaServiceAPI.limpiarClavesIdempotencia();
        logger.info("tarea=mantenimiento pagos_vencidos=" + vencidas + " claves_eliminadas=" + claves);
    }
}
