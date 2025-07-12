package tragsatec.pes.persistence.repository.general;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tragsatec.pes.dto.general.DemarcacionProjection;
import tragsatec.pes.persistence.entity.general.DemarcacionEntity;

import java.util.List;

public interface DemarcacionRepository extends JpaRepository<DemarcacionEntity, Integer> {
    @Query(value = "SELECT id, codigo, nombre FROM demarcacion WHERE tipo = :tipo", nativeQuery = true)
    List<DemarcacionProjection> findDemarcacionesByTipo(@Param("tipo") Character tipo);
}