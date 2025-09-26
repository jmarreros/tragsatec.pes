package com.chc.pes.persistence.repository.calculo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.chc.pes.dto.calculo.IndicadorDataProjection;
import com.chc.pes.persistence.entity.calculo.IndicadorSequiaEntity;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface IndicadorSequiaRepository extends JpaRepository<IndicadorSequiaEntity, Long> {
    void deleteByMedicionId(Integer medicionId);

    List<IndicadorSequiaEntity> findByMedicionId(Integer medicionId);

    // Suma de prep1 de los últimos N meses para cada estación
    @Query(value = "WITH RankedIndicadores AS ( " +
            "    SELECT " +
            "        estacion_id, " +
            "        prep1, " +
            "        ROW_NUMBER() OVER(PARTITION BY estacion_id ORDER BY anio DESC, mes DESC) as rn " +
            "    FROM " +
            "        indicador_sequia " +
            ") " +
            "SELECT " +
            "    estacion_id, " +
            "    CASE " +
            "        WHEN COUNT(CASE WHEN prep1 IS NULL THEN 1 END) > 0 THEN NULL " +
            "        ELSE SUM(prep1) " +
            "    END AS suma_ultimos_n_prep1 " +
            "FROM " +
            "    RankedIndicadores " +
            "WHERE " +
            "    rn <= :numMeses " +
            "GROUP BY " +
            "    estacion_id", nativeQuery = true)
    List<Object[]> sumLastNPrep1ForEachEstacion(@Param("numMeses") Integer numMeses);

    // Inserta estaciones faltantes de sequia con valor 0 para una medición dada, las estaciones activas se obtienen desde pes_ut_estacion
    @Modifying
    @Transactional
    @Query(value = "INSERT INTO detalle_medicion (estacion_id, valor, medicion_id) " +
            "SELECT DISTINCT estacion_id, NULL, :medicionId " +
            "FROM pes_ut_estacion " +
            "WHERE tipo = 'S' AND " +
            "pes_id = :pesId AND " +
            "estacion_id NOT IN (SELECT estacion_id FROM detalle_medicion WHERE medicion_id = :medicionId)",
            nativeQuery = true)
    void insertEstacionesFaltantes(@Param("medicionId") Integer medicionId, @Param("pesId") Integer pesId);


    @Query(value = "SELECT anio, mes, prep1 AS dato, ie_b1 AS indicador FROM indicador_sequia WHERE estacion_id = :estacionId AND anio <= :maxYear ORDER BY anio, mes", nativeQuery = true)
    List<IndicadorDataProjection> getAllDataIndicadorAnioMesPrep1(@Param("estacionId") Integer estacionId, @Param("maxYear") Integer maxYear);

    @Query(value = "SELECT anio, mes, prep3 AS dato, ie_b3 AS indicador FROM indicador_sequia WHERE estacion_id = :estacionId AND anio <= :maxYear ORDER BY anio, mes", nativeQuery = true)
    List<IndicadorDataProjection> getAllDataIndicadorAnioMesPrep3(@Param("estacionId") Integer estacionId, @Param("maxYear") Integer maxYear);



}