package tragsatec.pes.persistence.repository.calculo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tragsatec.pes.dto.calculo.IndicadorDataProjection;
import tragsatec.pes.dto.calculo.IndicadorFechaDataProjection;
import tragsatec.pes.persistence.entity.calculo.IndicadorUtEscasezEntity;

import java.util.List;

@Repository
public interface IndicadorUtEscasezRepository extends JpaRepository<IndicadorUtEscasezEntity, Long> {

    @Modifying
    @Query(value = "INSERT INTO indicador_ut_escasez(" +
            "    medicion_id, " +
            "    unidad_territorial_id, " +
            "    dato, " +
            "    ie, " +
            "    anio, " +
            "    mes" +
            ") " +
            "SELECT " +
            "    inde.medicion_id, " +
            "    pute.unidad_territorial_id, " +
            "    SUM(inde.dato * pute.coeficiente / 100.0), " +
            "    SUM(inde.ie * pute.coeficiente / 100.0), " +
            "    inde.anio, " +
            "    inde.mes " +
            "FROM indicador_escasez inde " +
            "INNER JOIN pes_ut_estacion pute ON inde.estacion_id = pute.estacion_id " +
            "WHERE inde.medicion_id = :medicionId " +
            "  AND pute.pes_id = :pesId " +
            "  AND pute.tipo = 'E' " +
            "GROUP BY inde.medicion_id, pute.unidad_territorial_id, inde.anio, inde.mes", nativeQuery = true)
    void insertIndicadorUtEscasez(
            @Param("medicionId") Integer medicionId,
            @Param("pesId") Integer pesId
    );

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


    @Modifying
    @Query("DELETE FROM IndicadorUtEscasezEntity i WHERE i.medicionId = :medicionId")
    void deleteByMedicionId(@Param("medicionId") Integer medicionId);
}