package tragsatec.pes.persistence.repository.general;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tragsatec.pes.dto.general.UnidadTerritorialProjection;
import tragsatec.pes.persistence.entity.general.UnidadTerritorialEntity;

import java.util.List;

public interface UnidadTerritorialRepository extends JpaRepository<UnidadTerritorialEntity, Integer> {

    @Query(value = "SELECT id, nombre, codigo FROM unidad_territorial WHERE tipo = :tipo ORDER BY codigo",
            nativeQuery = true)
    List<UnidadTerritorialProjection> findUnidadesTerritorialesByTipo(@Param("tipo") Character tipo);
}