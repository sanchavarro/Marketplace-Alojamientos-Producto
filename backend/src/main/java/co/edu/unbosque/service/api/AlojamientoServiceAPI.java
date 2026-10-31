package co.edu.unbosque.service.api;

import co.edu.unbosque.dto.AlojamientoDTO;
import co.edu.unbosque.dto.AlojamientoRequest;
import co.edu.unbosque.dto.AlojamientoResumenDTO;
import co.edu.unbosque.dto.ImagenRequest;
import co.edu.unbosque.dto.PaginaResponse;
import co.edu.unbosque.dto.ServicioAdicionalRequest;
import co.edu.unbosque.entity.Alojamiento;
import co.edu.unbosque.entity.Imagen;
import co.edu.unbosque.entity.ServicioAdicional;
import co.edu.unbosque.utils.GenericServiceAPI;

/**
 * IPublicaciones del diagrama de componentes (épica E03, HU-07 a HU-15).
 */
public interface AlojamientoServiceAPI extends GenericServiceAPI<Alojamiento, Long> {

    Alojamiento obtener(Long id);

    Alojamiento obtenerPropio(Long anfitrionId, Long alojamientoId);

    AlojamientoDTO crear(Long anfitrionId, AlojamientoRequest request);

    AlojamientoDTO editar(Long anfitrionId, Long alojamientoId, AlojamientoRequest request);

    AlojamientoDTO detalle(Long alojamientoId, Long usuarioId, String rol);

    PaginaResponse<AlojamientoResumenDTO> misAlojamientos(Long anfitrionId, Integer pagina, Integer tamano);

    AlojamientoDTO cambiarEstadoPorAnfitrion(Long anfitrionId, Long alojamientoId, String accion);

    AlojamientoDTO cambiarEstadoPorAdministrador(Long adminId, Long alojamientoId, String nuevoEstado);

    PaginaResponse<AlojamientoResumenDTO> pendientesDeRevision(Integer pagina, Integer tamano);

    Imagen agregarImagen(Long anfitrionId, Long alojamientoId, ImagenRequest request);

    void eliminarImagen(Long anfitrionId, Long alojamientoId, Long imagenId);

    ServicioAdicional agregarServicioAdicional(Long anfitrionId, Long alojamientoId, ServicioAdicionalRequest request);

    void eliminarServicioAdicional(Long anfitrionId, Long alojamientoId, Long servicioAdicionalId);

    AlojamientoResumenDTO aResumen(Alojamiento alojamiento);
}
