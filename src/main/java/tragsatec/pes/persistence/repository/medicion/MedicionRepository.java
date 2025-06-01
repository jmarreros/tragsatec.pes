package tragsatec.pes.persistence.repository.medicion;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tragsatec.pes.persistence.entity.medicion.MedicionEntity;

import java.util.List;

@Repository
public interface MedicionRepository extends JpaRepository<MedicionEntity, Integer> {
     List<MedicionEntity> findByPesIdAndTipoAndAnioAndMesAndEliminadoFalse(Integer pesId, Character tipo, Short anio, Byte mes);
}

