package tragsatec.pes.persistence.repository.calculo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tragsatec.pes.persistence.entity.calculo.IndicadorSequiaEntity;

@Repository
public interface IndicadorSequiaRepository extends JpaRepository<IndicadorSequiaEntity, Long> {
}
