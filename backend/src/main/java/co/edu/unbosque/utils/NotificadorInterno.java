package co.edu.unbosque.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import co.edu.unbosque.service.api.NotificacionServiceAPI;

/**
 * Crea el canal "interno": la notificación queda en la tabla notificacion y el usuario la ve en la app (HU-36).
 */
@Component
public class NotificadorInterno extends Notificador {

    @Autowired
    private NotificacionServiceAPI notificacionServiceAPI;

    @Override
    protected CanalNotificacion crearCanal() {
        return (usuarioId, tipoEvento, titulo, mensaje) ->
                notificacionServiceAPI.crear(usuarioId, tipoEvento, titulo + ": " + mensaje);
    }
}
