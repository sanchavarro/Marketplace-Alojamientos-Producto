package co.edu.unbosque.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import co.edu.unbosque.entity.Reserva;

@Repository
public interface ReservaRepository extends CrudRepository<Reserva, Long>, PagingAndSortingRepository<Reserva, Long> {

    /** HU-29: historial del huésped. */
    Page<Reserva> findByHuespedIdOrderByCreadaEnDesc(Long huespedId, Pageable pageable);

    /** HU-30: reservas de los alojamientos de un anfitrión. */
    @Query("SELECT r FROM Reserva r WHERE r.alojamientoId IN "
            + "(SELECT a.id FROM Alojamiento a WHERE a.anfitrionId = :anfitrionId) ORDER BY r.fechaLlegada DESC")
    Page<Reserva> buscarPorAnfitrion(@Param("anfitrionId") Long anfitrionId, Pageable pageable);

    List<Reserva> findByEstadoAndFechaSalidaLessThanEqual(String estado, LocalDate fecha);

    List<Reserva> findByEstadoAndCreadaEnBefore(String estado, LocalDateTime fecha);

    List<Reserva> findByCreadaEnBetweenOrderByCreadaEnAsc(LocalDateTime desde, LocalDateTime hasta);

    long countByEstado(String estado);
}
