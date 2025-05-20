package tragsatec.pes.persistence.repository.medicion;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tragsatec.pes.persistence.entity.medicion.MedicionEntity;

@Repository
public interface MedicionRepository extends JpaRepository<MedicionEntity, Integer> {
}

