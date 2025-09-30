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
import com.chc.pes.persistence.entity.calculo.IndicadorUtEscasezEntity;

import java.util.List;

@Repository
public interface IndicadorUtEscasezRepository extends JpaRepository<IndicadorUtEscasezEntity, Long> {

    @Query(value = "SELECT anio, mes, dato, ie AS indicador FROM indicador_ut_escasez WHERE unidad_territorial_id = :utId ORDER BY anio, mes", nativeQuery = true)
    List<IndicadorDataProjection> getAllDataIndicadorAnioMes(@Param("utId") Integer utId);


    @Query(value = "SELECT ut.nombre, iut.anio, iut.mes, iut.dato, iut.ie AS indicador " +
            "FROM indicador_ut_escasez iut " +
            "INNER JOIN unidad_territorial ut ON iut.unidad_territorial_id = ut.id " +
            "WHERE ((iut.anio = :startYear AND iut.mes >= :startMonth) OR (iut.anio = :endYear AND iut.mes <= :endMonth)) " +
            "ORDER BY ut.nombre, iut.anio, iut.mes", nativeQuery = true)
    List<IndicadorFechaDataProjection> getAllDataFecha(
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
            "iute.anio, " +
            "iute.mes, " +
            "iute.ie indicador " +
            "FROM " +
            "pes_demarcacion_ut pdut " +
            "INNER JOIN demarcacion d ON pdut.demarcacion_id = d.id " +
            "INNER JOIN unidad_territorial ut ON ut.id = pdut.unidad_territorial_id " +
            "INNER JOIN indicador_ut_escasez iute ON iute.unidad_territorial_id = ut.id " +
            "WHERE pdut.pes_id = :pesId AND d.id = :demarcacionId AND ( " +
            "(iute.anio = :startYear AND iute.mes >= :startMonth) OR (iute.anio = :endYear AND iute.mes <= :endMonth)) " +
            "ORDER BY ut_codigo, iute.anio, iute.mes", nativeQuery = true)
    List<IndicadorDemarcacionFechaDataProjection> getAllDataFechaDemarcacion(
            @Param("pesId") Integer pesId,
            @Param("demarcacionId") Integer demarcacionId,
            @Param("startYear") Integer startYear,
            @Param("startMonth") Integer startMonth,
            @Param("endYear") Integer endYear,
            @Param("endMonth") Integer endMonth
    );

    @Query(value = "SELECT ut.id, ut.codigo, ut.nombre, anio, mes, ie indicador, dato valor " +
            "FROM indicador_ut_escasez iut " +
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

    @Query(value = "SELECT e.id, e.codigo, e.nombre, i.anio, i.mes, i.ie indicador, i.dato valor " +
            "FROM pes_ut_estacion pesut " +
            "INNER JOIN estacion e ON pesut.estacion_id = e.id " +
            "INNER JOIN indicador_escasez i ON i.estacion_id = e.id " +
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

    List<IndicadorUtEscasezEntity> findByMedicionId(Integer medicionId);

    @Modifying
    @Query("DELETE FROM IndicadorUtEscasezEntity i WHERE i.medicionId = :medicionId")
    void deleteByMedicionId(@Param("medicionId") Integer medicionId);
}