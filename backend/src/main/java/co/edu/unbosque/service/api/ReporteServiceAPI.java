package co.edu.unbosque.service.api;

import java.time.LocalDate;
import java.util.Map;

/**
 * IReportes del diagrama de componentes (épicas E12 y E13, HU-46 a HU-48).
 */
public interface ReporteServiceAPI {

    Map<String, Object> dashboard();

    /** Exporta las reservas creadas entre dos fechas en "csv" (Excel) o "pdf". Registra quién exporta (HU-48). */
    byte[] exportarReservas(Long adminId, String formato, LocalDate desde, LocalDate hasta);
}
