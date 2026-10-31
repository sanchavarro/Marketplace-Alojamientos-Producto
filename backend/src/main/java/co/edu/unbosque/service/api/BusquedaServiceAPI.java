package co.edu.unbosque.service.api;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import co.edu.unbosque.dto.AlojamientoResumenDTO;
import co.edu.unbosque.dto.PaginaResponse;

/**
 * IBusqueda del diagrama de componentes (épica E05, HU-19 a HU-22, ADR-08).
 */
public interface BusquedaServiceAPI {

    PaginaResponse<AlojamientoResumenDTO> buscar(String ciudad, String tipo, Integer capacidad,
                                                 BigDecimal precioMin, BigDecimal precioMax,
                                                 LocalDate llegada, LocalDate salida, List<Long> servicios,
                                                 Integer pagina, Integer tamano);

    List<AlojamientoResumenDTO> comparar(List<Long> ids);
}
