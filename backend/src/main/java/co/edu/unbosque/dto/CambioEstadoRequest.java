package co.edu.unbosque.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Acción sobre el estado de una publicación (HU-09, HU-10).
 */
public class CambioEstadoRequest {

    @NotBlank(message = "La acción es obligatoria (ENVIAR_REVISION, PAUSAR, ACTIVAR).")
    private String accion;

    public CambioEstadoRequest() {
    }

    public String getAccion() { return accion; }
    public void setAccion(String accion) { this.accion = accion; }
}
