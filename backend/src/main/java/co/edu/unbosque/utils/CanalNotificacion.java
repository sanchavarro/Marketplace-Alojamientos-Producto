package co.edu.unbosque.utils;

/**
 * Producto del Factory Method: el medio por el que llega el aviso al usuario.
 */
public interface CanalNotificacion {

    void enviar(Long usuarioId, String tipoEvento, String titulo, String mensaje);
}
