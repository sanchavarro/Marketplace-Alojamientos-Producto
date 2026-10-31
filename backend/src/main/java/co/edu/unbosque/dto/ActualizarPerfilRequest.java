package co.edu.unbosque.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Cambios del perfil (HU-05). La clave solo cambia si se envían claveActual y nuevaClave.
 */
public class ActualizarPerfilRequest {

    @NotBlank(message = "El nombre es obligatorio.")
    @Size(max = 120, message = "El nombre admite máximo 120 caracteres.")
    private String nombre;

    private String claveActual;

    @Size(min = 8, max = 64, message = "La clave debe tener entre 8 y 64 caracteres.")
    private String nuevaClave;

    public ActualizarPerfilRequest() {
    }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getClaveActual() { return claveActual; }
    public void setClaveActual(String claveActual) { this.claveActual = claveActual; }

    public String getNuevaClave() { return nuevaClave; }
    public void setNuevaClave(String nuevaClave) { this.nuevaClave = nuevaClave; }
}
