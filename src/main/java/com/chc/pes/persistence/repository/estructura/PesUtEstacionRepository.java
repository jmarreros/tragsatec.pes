package com.chc.pes.persistence.repository.estructura;

import com.chc.pes.dto.general.EstacionPesUtProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.chc.pes.dto.general.EstacionProjection;
import com.chc.pes.persistence.entity.estructura.PesUtEstacionEntity;

import java.util.List;

@Repository
public interface PesUtEstacionRepository extends JpaRepository<PesUtEstacionEntity, Integer> {
    @Query("SELECT DISTINCT e.id AS id, e.codigo AS codigo " +
            "FROM PesUtEstacionEntity pute JOIN pute.estacion e " +
            "WHERE pute.pes.id = :pesId AND pute.tipo = :tipo")
    List<EstacionProjection> getAllEstacionesByPesId(@Param("pesId") Integer pesId,
                                                     @Param("tipo") Character tipo);

    List<PesUtEstacionEntity> findByPesIdAndTipo(Integer pesId, Character tipo);


    @Query(value = "SELECT e.id AS id, e.codigo AS codigo, e.nombre AS nombre, " +
            "pute.coeficiente AS coeficiente, e.coordenadas AS coordenadas " +
            "FROM pes_ut_estacion pute " +
            "INNER JOIN estacion e ON pute.estacion_id = e.id " +
            "WHERE pute.tipo = :tipo AND pute.unidad_territorial_id = :unidadId " +
            "ORDER BY pute.orden",
            nativeQuery = true)
    List<EstacionPesUtProjection> findEstacionesPesIdWithCoeficienteByTipoAndUT(
            @Param("tipo") Character tipo,
            @Param("unidadId") Integer unidadTerritorialId);

}

