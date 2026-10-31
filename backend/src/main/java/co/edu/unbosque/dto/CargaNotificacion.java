package co.edu.unbosque.dto;

/**
 * Contenido JSON de un evento del outbox (ADR-07).
 */
public class CargaNotificacion {

    private Long usuarioId;

    private String titulo;

    private String mensaje;

    public CargaNotificacion() {
    }

    public Long getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Long usuarioId) { this.usuarioId = usuarioId; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }
}
