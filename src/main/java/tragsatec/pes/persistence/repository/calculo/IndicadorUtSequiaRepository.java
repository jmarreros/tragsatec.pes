package tragsatec.pes.persistence.repository.calculo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tragsatec.pes.dto.calculo.IndicadorDataProjection;
import tragsatec.pes.persistence.entity.calculo.IndicadorUtSequiaEntity;

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


    @Modifying
    @Query("DELETE FROM IndicadorUtSequiaEntity i WHERE i.medicionId = :medicionId")
    void deleteByMedicionId(@Param("medicionId") Integer medicionId);
}