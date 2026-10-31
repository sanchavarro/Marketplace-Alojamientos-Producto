package co.edu.unbosque.repository;

import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import co.edu.unbosque.entity.Cancelacion;

@Repository
public interface CancelacionRepository extends CrudRepository<Cancelacion, Long> {

    Optional<Cancelacion> findByReservaId(Long reservaId);
}
