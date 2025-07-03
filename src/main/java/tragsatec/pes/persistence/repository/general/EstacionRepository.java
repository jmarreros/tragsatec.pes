package tragsatec.pes.persistence.repository.general;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tragsatec.pes.dto.estructura.EstacionProjection;
import tragsatec.pes.persistence.entity.general.EstacionEntity;

import java.util.List;
import java.util.Optional;

public interface EstacionRepository extends JpaRepository<EstacionEntity, Integer> {
    boolean existsByCodigo(String codigo);

    @Query(value = "SELECT DISTINCT pe.estacion_id as id, e.codigo as codigo " +
            "FROM pes_ut_estacion pe " +
            "INNER JOIN estacion e ON pe.estacion_id = e.id " +
            "WHERE pe.tipo = :tipo AND pe.pes_id = :pesId ORDER BY e.codigo",
            nativeQuery = true)
    List<EstacionProjection> findEstacionesByPes(
            @Param("pesId") Optional<Integer> pesId,
            @Param("tipo") Character tipo);

}