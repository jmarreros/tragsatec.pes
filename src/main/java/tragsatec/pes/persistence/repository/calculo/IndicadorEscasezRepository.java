package tragsatec.pes.persistence.repository.calculo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tragsatec.pes.persistence.entity.calculo.IndicadorEscasezEntity;

@Repository
public interface IndicadorEscasezRepository extends JpaRepository<IndicadorEscasezEntity, Long> {
    void deleteByMedicionId(Integer medicionId);
}

