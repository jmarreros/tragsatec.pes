package com.chc.pes.persistence.repository.calculo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.chc.pes.dto.calculo.IndicadorDataProjection;
import com.chc.pes.persistence.entity.calculo.IndicadorEscasezEntity;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface IndicadorEscasezRepository extends JpaRepository<IndicadorEscasezEntity, Long> {
    void deleteByMedicionId(Integer medicionId);

    // Inserta estaciones faltantes de escasez con valor 0 para una medición dada, las estaciones activas se obtienen desde pes_ut_estacion
    @Modifying
    @Transactional
    @Query(value = "INSERT INTO detalle_medicion (estacion_id, valor, medicion_id) " +
            "SELECT DISTINCT estacion_id, 0, :medicionId " +
            "FROM pes_ut_estacion " +
            "WHERE tipo = 'E' AND " +
            "pes_id = :pesId AND " +
            "estacion_id NOT IN (SELECT estacion_id FROM detalle_medicion WHERE medicion_id = :medicionId)",
            nativeQuery = true)
    void insertEstacionesFaltantes(@Param("medicionId") Integer medicionId, @Param("pesId") Integer pesId);


    @Query(value = "SELECT anio, mes, dato, ie AS indicador FROM indicador_escasez WHERE estacion_id = :estacionId AND anio <= :maxYear ORDER BY anio, mes", nativeQuery = true)
    List<IndicadorDataProjection> getAllDataIndicadorAnioMes(@Param("estacionId") Integer estacionId, @Param("maxYear") Integer maxYear);
}