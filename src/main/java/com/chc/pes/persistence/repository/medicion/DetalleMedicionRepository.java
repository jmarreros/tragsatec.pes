package com.chc.pes.persistence.repository.medicion;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.chc.pes.dto.medicion.DetalleMedicionProjection;
import com.chc.pes.persistence.entity.medicion.DetalleMedicionEntity;

import java.util.List;

@Repository
public interface DetalleMedicionRepository extends JpaRepository<DetalleMedicionEntity, Long> {
    List<DetalleMedicionEntity> findByMedicionId(Integer medicionId);

    @Query(value = "SELECT e.codigo as codigo, dm.valor as valor " +
            "FROM detalle_medicion dm " +
            "INNER JOIN estacion e ON dm.estacion_id = e.id " +
            "WHERE dm.medicion_id = :medicionId " +
            "ORDER BY e.codigo",
            nativeQuery = true)
    List<DetalleMedicionProjection> findReporteByMedicionId(@Param("medicionId") Integer medicionId);


    @Query("SELECT dm FROM DetalleMedicionEntity dm " +
           "WHERE dm.medicion.id = :medicionId AND dm.estacion.id = :estacionId")
    java.util.Optional<DetalleMedicionEntity> findByMedicionIdAndEstacionId(@Param("medicionId") Integer medicionId, @Param("estacionId") Integer estacionId);
}
