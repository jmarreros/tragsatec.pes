package com.chc.pes.persistence.repository.calculo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.chc.pes.dto.calculo.IndicadorDataProjection;
import com.chc.pes.dto.calculo.IndicadorDemarcacionFechaDataProjection;
import com.chc.pes.dto.calculo.IndicadorFechaDataProjection;
import com.chc.pes.dto.calculo.IndicadorUTFechaDataProjection;
import com.chc.pes.persistence.entity.calculo.IndicadorUtSequiaEntity;

import java.util.List;

@Repository
public interface IndicadorUtSequiaRepository extends JpaRepository<IndicadorUtSequiaEntity, Long> {

    @Modifying
    @Query(value = "INSERT INTO indicador_ut_sequia (" +
            "    medicion_id, " +
            "    unidad_territorial_id, " +
            "    prep1, " +
            "    prep3, " +
            "    prep6, " +
            "    ie_b1, " +
            "    ie_b3, " +
            "    ie_b6, " +
            "    anio, " +
            "    mes" +
            ") " +
            "SELECT " +
            "    inds.medicion_id, " +
            "    pute.unidad_territorial_id, " +
            "    SUM(inds.prep1 * pute.coeficiente / 100.0), " +
            "    SUM(inds.prep3 * pute.coeficiente / 100.0), " +
            "    SUM(inds.prep6 * pute.coeficiente / 100.0), " +
            "    SUM(inds.ie_b1 * pute.coeficiente / 100.0), " +
            "    SUM(inds.ie_b3 * pute.coeficiente / 100.0), " +
            "    SUM(inds.ie_b6 * pute.coeficiente / 100.0), " +
            "    inds.anio, " +
            "    inds.mes " +
            "FROM indicador_sequia inds " +
            "INNER JOIN pes_ut_estacion pute ON inds.estacion_id = pute.estacion_id " +
            "WHERE inds.medicion_id = :medicionId " +
            "  AND pute.pes_id = :pesId " +
            "  AND pute.tipo = 'S' " +
            "GROUP BY inds.medicion_id, pute.unidad_territorial_id, inds.anio, inds.mes", nativeQuery = true)
    void insertIndicadorUtSequia(
            @Param("medicionId") Integer medicionId,
            @Param("pesId") Integer pesId
    );


    @Query(value = "SELECT anio, mes, prep1 AS dato, ie_b1 AS indicador FROM indicador_ut_sequia WHERE unidad_territorial_id = :utId ORDER BY anio, mes", nativeQuery = true)
    List<IndicadorDataProjection> getAllDataIndicadorAnioMesPrep1(@Param("utId") Integer utId);

    @Query(value = "SELECT anio, mes, prep3 AS dato, ie_b3 AS indicador FROM indicador_ut_sequia WHERE unidad_territorial_id = :utId ORDER BY anio, mes", nativeQuery = true)
    List<IndicadorDataProjection> getAllDataIndicadorAnioMesPrep3(@Param("utId") Integer utId);


    @Query(value = "SELECT ut.nombre, iut.anio, iut.mes, iut.prep1 AS dato, iut.ie_b1 AS indicador " +
            "FROM indicador_ut_sequia iut " +
            "INNER JOIN unidad_territorial ut ON iut.unidad_territorial_id = ut.id " +
            "WHERE ((iut.anio = :startYear AND iut.mes >= :startMonth) OR (iut.anio = :endYear AND iut.mes <= :endMonth)) " +
            "ORDER BY ut.nombre, iut.anio, iut.mes", nativeQuery = true)
    List<IndicadorFechaDataProjection> getAllDataFechaPrep1(
            @Param("startYear") Integer startYear,
            @Param("startMonth") Integer startMonth,
            @Param("endYear") Integer endYear,
            @Param("endMonth") Integer endMonth
    );

    @Query(value = "SELECT ut.nombre, iut.anio, iut.mes, iut.prep3 AS dato, iut.ie_b3 AS indicador " +
            "FROM indicador_ut_sequia iut " +
            "INNER JOIN unidad_territorial ut ON iut.unidad_territorial_id = ut.id " +
            "WHERE ((iut.anio = :startYear AND iut.mes >= :startMonth) OR (iut.anio = :endYear AND iut.mes <= :endMonth)) " +
            "ORDER BY ut.nombre, iut.anio, iut.mes", nativeQuery = true)
    List<IndicadorFechaDataProjection> getAllDataFechaPrep3(
            @Param("startYear") Integer startYear,
            @Param("startMonth") Integer startMonth,
            @Param("endYear") Integer endYear,
            @Param("endMonth") Integer endMonth
    );

    @Query(value = "SELECT " +
            "d.codigo d_codigo, " +
            "d.nombre d_nombre, " +
            "ut.id ut_id, " +
            "ut.codigo ut_codigo, " +
            "ut.nombre ut_nombre, " +
            "iuts.anio, " +
            "iuts.mes, " +
            "iuts.ie_b1 indicador " +
            "FROM " +
            "pes_demarcacion_ut pdut " +
            "INNER JOIN demarcacion d ON pdut.demarcacion_id = d.id " +
            "INNER JOIN unidad_territorial ut ON ut.id = pdut.unidad_territorial_id " +
            "INNER JOIN indicador_ut_sequia iuts ON iuts.unidad_territorial_id = ut.id " +
            "WHERE pdut.pes_id = :pesId AND d.id = :demarcacionId AND ( " +
            "(iuts.anio = :startYear AND iuts.mes >= :startMonth) OR (iuts.anio = :endYear AND iuts.mes <= :endMonth)) " +
            "ORDER BY ut_codigo, iuts.anio, iuts.mes", nativeQuery = true)
    List<IndicadorDemarcacionFechaDataProjection> getAllDataFechaDemarcacionPrep1(
            @Param("pesId") Integer pesId,
            @Param("demarcacionId") Integer demarcacionId,
            @Param("startYear") Integer startYear,
            @Param("startMonth") Integer startMonth,
            @Param("endYear") Integer endYear,
            @Param("endMonth") Integer endMonth
    );

    @Query(value = "SELECT " +
            "d.codigo d_codigo, " +
            "d.nombre d_nombre, " +
            "ut.id ut_id, " +
            "ut.codigo ut_codigo, " +
            "ut.nombre ut_nombre, " +
            "iuts.anio, " +
            "iuts.mes, " +
            "iuts.ie_b3 indicador " +
            "FROM " +
            "pes_demarcacion_ut pdut " +
            "INNER JOIN demarcacion d ON pdut.demarcacion_id = d.id " +
            "INNER JOIN unidad_territorial ut ON ut.id = pdut.unidad_territorial_id " +
            "INNER JOIN indicador_ut_sequia iuts ON iuts.unidad_territorial_id = ut.id " +
            "WHERE pdut.pes_id = :pesId AND d.id = :demarcacionId AND ( " +
            "(iuts.anio = :startYear AND iuts.mes >= :startMonth) OR (iuts.anio = :endYear AND iuts.mes <= :endMonth)) " +
            "ORDER BY ut_codigo, iuts.anio, iuts.mes", nativeQuery = true)
    List<IndicadorDemarcacionFechaDataProjection> getAllDataFechaDemarcacionPrep3(
            @Param("pesId") Integer pesId,
            @Param("demarcacionId") Integer demarcacionId,
            @Param("startYear") Integer startYear,
            @Param("startMonth") Integer startMonth,
            @Param("endYear") Integer endYear,
            @Param("endMonth") Integer endMonth
    );

    @Query(value = "SELECT ut.id, ut.codigo, ut.nombre, anio, mes, ie_b1 indicador, prep1 valor " +
            "FROM indicador_ut_sequia iut " +
            "INNER JOIN unidad_territorial ut ON iut.unidad_territorial_id = ut.id " +
            "WHERE ut.id = :utId AND ((iut.anio = :startYear AND iut.mes >= :startMonth) OR (iut.anio = :endYear AND iut.mes <= :endMonth)) " +
            "ORDER BY ut.nombre, iut.anio, iut.mes", nativeQuery = true)
    List<IndicadorUTFechaDataProjection> getTotalDataUTFecha(
            @Param("utId") Integer utId,
            @Param("startYear") Integer startYear,
            @Param("startMonth") Integer startMonth,
            @Param("endYear") Integer endYear,
            @Param("endMonth") Integer endMonth
    );

    @Query(value = "SELECT e.id, e.codigo, e.nombre, i.anio, i.mes, i.ie_b1 indicador, i.prep1 valor " +
            "FROM pes_ut_estacion pesut " +
            "INNER JOIN estacion e ON pesut.estacion_id = e.id " +
            "INNER JOIN indicador_sequia i ON i.estacion_id = e.id " +
            "WHERE pesut.pes_id = :pesId AND pesut.unidad_territorial_id = :utId AND ((i.anio = :startYear AND i.mes >= :startMonth) OR (i.anio = :endYear AND i.mes <= :endMonth)) " +
            "ORDER BY e.nombre, i.anio, i.mes", nativeQuery = true)
    List<IndicadorUTFechaDataProjection> getUTEstacionFecha(
            @Param("pesId") Integer pesId,
            @Param("utId") Integer utId,
            @Param("startYear") Integer startYear,
            @Param("startMonth") Integer startMonth,
            @Param("endYear") Integer endYear,
            @Param("endMonth") Integer endMonth
    );

    @Modifying
    @Query("DELETE FROM IndicadorUtSequiaEntity i WHERE i.medicionId = :medicionId")
    void deleteByMedicionId(@Param("medicionId") Integer medicionId);
}