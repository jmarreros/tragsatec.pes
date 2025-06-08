package tragsatec.pes.persistence.repository.calculo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tragsatec.pes.persistence.entity.calculo.IndicadorSequiaEntity;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface IndicadorSequiaRepository extends JpaRepository<IndicadorSequiaEntity, Long> {
    void deleteByMedicionId(Integer medicionId);

    List<IndicadorSequiaEntity> findByMedicionId(Integer medicionId);
}
