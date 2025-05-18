package tragsatec.pes.persistence.repository.general;

import org.springframework.data.jpa.repository.JpaRepository;
import tragsatec.pes.persistence.entity.general.EstacionEntity;

public interface EstacionRepository extends JpaRepository<EstacionEntity, Integer> {
    boolean existsByCodigo(String codigo);
}