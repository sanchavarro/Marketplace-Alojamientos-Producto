package co.edu.unbosque.utils;

import java.util.logging.Logger;
import org.springframework.stereotype.Component;

/**
 * Crea el canal "correo". En el MVP el envío es simulado: se registra en el log con formato estructurado.
 * Para enviar correos reales solo se cambia este canal (por ejemplo con spring-boot-starter-mail).
 */
@Component
public class NotificadorCorreo extends Notificador {

    private static final Logger logger = Logger.getLogger(NotificadorCorreo.class.getName());

    @Override
    protected CanalNotificacion crearCanal() {
        return (usuarioId, tipoEvento, titulo, mensaje) ->
                logger.info("canal=correo_simulado usuario=" + usuarioId + " evento=" + tipoEvento
                        + " asunto=\"" + titulo + "\"");
    }
}
