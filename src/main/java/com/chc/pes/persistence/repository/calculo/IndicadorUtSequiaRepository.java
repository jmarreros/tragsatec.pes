package com.chc.pes.persistence.repository.calculo;

import com.chc.pes.dto.calculo.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.chc.pes.persistence.entity.calculo.IndicadorUtSequiaEntity;

import java.util.List;

@Repository
public interface IndicadorUtSequiaRepository extends JpaRepository<IndicadorUtSequiaEntity, Long> {

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
            "iuts.ie_b1 indicador, " +
            "iuts.escenario_final " +
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
            "iuts.ie_b3 indicador, " +
            "iuts.escenario_final " +
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

    @Query(value = "SELECT ut.id, ut.codigo, ut.nombre, iut.anio, iut.mes, iut.ie_b3 indicador, iut.prep3 valor, iut.escenario_final " +
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

    @Query(value = "SELECT e.id, e.codigo, e.nombre, i.anio, i.mes, i.ie_b3 indicador, i.prep1 valor " +
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

    List<IndicadorUtSequiaEntity> findByMedicionId(Integer medicionId);

    @Modifying
    @Query("DELETE FROM IndicadorUtSequiaEntity i WHERE i.medicionId = :medicionId")
    void deleteByMedicionId(@Param("medicionId") Integer medicionId);


    @Query(value = "SELECT ut.id AS id, ut.codigo_dh AS codigoDh, ut.nombre AS nombre, iut.escenario_final AS escenarioFinal " +
            "FROM indicador_ut_sequia iut " +
            "INNER JOIN unidad_territorial ut ON iut.unidad_territorial_id = ut.id " +
            "WHERE iut.anio = :anio AND iut.mes = :mes AND ut.demarcacion_id = :demarcacionId",
            nativeQuery = true)
    List<IndicadorUTEscenarioProjection> findByAnioMesDemarcacionUTEscenario(
            @Param("anio") Integer anio,
            @Param("mes") Integer mes,
            @Param("demarcacionId") Integer demarcacionId
    );
}