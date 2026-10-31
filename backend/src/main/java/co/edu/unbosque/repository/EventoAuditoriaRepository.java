package co.edu.unbosque.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
import co.edu.unbosque.entity.EventoAuditoria;

/**
 * Solo se usan save y consultas: la auditoría no se edita ni se borra (ADR-06).
 */
@Repository
public interface EventoAuditoriaRepository extends CrudRepository<EventoAuditoria, Long>,
        PagingAndSortingRepository<EventoAuditoria, Long> {

    Page<EventoAuditoria> findAllByOrderByFechaDesc(Pageable pageable);

    Page<EventoAuditoria> findByEntidadOrderByFechaDesc(String entidad, Pageable pageable);

    Page<EventoAuditoria> findByUsuarioIdOrderByFechaDesc(Long usuarioId, Pageable pageable);
}
