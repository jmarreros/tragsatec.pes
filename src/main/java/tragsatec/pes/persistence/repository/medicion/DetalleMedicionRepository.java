package tragsatec.pes.persistence.repository.medicion;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tragsatec.pes.persistence.entity.medicion.DetalleMedicionEntity;

@Repository
public interface DetalleMedicionRepository extends JpaRepository<DetalleMedicionEntity, Integer> {
}

