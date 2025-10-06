package com.chc.pes.persistence.repository.general;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.chc.pes.dto.general.EstacionProjection;
import com.chc.pes.persistence.entity.general.EstacionEntity;

import java.util.List;
import java.util.Optional;

public interface EstacionRepository extends JpaRepository<EstacionEntity, Integer> {
    boolean existsByCodigo(String codigo);

    // A nivel de relación con PES
    @Query(value = "SELECT DISTINCT pe.estacion_id as id, e.codigo as codigo " +
            "FROM pes_ut_estacion pe " +
            "INNER JOIN estacion e ON pe.estacion_id = e.id " +
            "WHERE pe.tipo = :tipo AND pe.pes_id = :pesId ORDER BY e.codigo",
            nativeQuery = true)
    List<EstacionProjection> findEstacionesByPes(
            @Param("pesId") Optional<Integer> pesId,
            @Param("tipo") Character tipo);

    // A nivel de relación con unidad territorial
    @Query(value = "SELECT DISTINCT e.id, " +
            "e.codigo, " +
            "CONCAT(e.codigo, ' - ',  e.nombre) AS nombre " +
            "FROM estacion e " +
            "INNER JOIN estacion_ut eut ON e.id = eut.estacion_id " +
            "INNER JOIN unidad_territorial ut ON ut.id = eut.unidad_territorial_id " +
            "WHERE ut.tipo = :tipo ORDER BY e.codigo",
            nativeQuery = true)
    List<EstacionProjection> findEstacionesByTipo(@Param("tipo") Character tipo);
}