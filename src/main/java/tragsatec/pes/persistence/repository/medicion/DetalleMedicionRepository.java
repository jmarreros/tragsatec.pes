package tragsatec.pes.persistence.repository.medicion;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tragsatec.pes.persistence.entity.medicion.DetalleMedicionEntity;

import java.util.List;

@Repository
public interface DetalleMedicionRepository extends JpaRepository<DetalleMedicionEntity, Long> {
    List<DetalleMedicionEntity> findByMedicionId(Integer medicionId);
}
