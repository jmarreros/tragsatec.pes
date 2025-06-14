package tragsatec.pes.persistence.repository.calculo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tragsatec.pes.persistence.entity.calculo.IndicadorDhSequiaEntity;

@Repository
public interface IndicadorDhSequiaRepository extends JpaRepository<IndicadorDhSequiaEntity, Long> {

    @Modifying
    @Query(value = "INSERT INTO indicador_dh_sequia (" +
            "    medicion_id, " +
            "    demarcacion_id, " +
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
            "    iuts.medicion_id, " +
            "    ut.demarcacion_id, " +
            "    SUM(iuts.prep1 * pdut.coeficiente / 100.0), " +
            "    SUM(iuts.prep3 * pdut.coeficiente / 100.0), " +
            "    SUM(iuts.prep6 * pdut.coeficiente / 100.0), " +
            "    SUM(iuts.ie_b1 * pdut.coeficiente / 100.0), " +
            "    SUM(iuts.ie_b3 * pdut.coeficiente / 100.0), " +
            "    SUM(iuts.ie_b6 * pdut.coeficiente / 100.0), " +
            "    iuts.anio, " +
            "    iuts.mes " +
            "FROM indicador_ut_sequia iuts " +
            "INNER JOIN unidad_territorial ut ON iuts.unidad_territorial_id = ut.id " +
            "INNER JOIN pes_demarcacion_ut pdut ON pdut.unidad_territorial_id = ut.id " +
            "WHERE iuts.medicion_id = :medicionId " +
            "  AND pdut.pes_id = :pesId " +
            "GROUP BY ut.demarcacion_id, iuts.medicion_id, iuts.anio, iuts.mes", nativeQuery = true)
    void insertIndicadorDhSequia(
            @Param("medicionId") Integer medicionId,
            @Param("pesId") Integer pesId
    );

    @Modifying
    @Query("DELETE FROM IndicadorDhSequiaEntity i WHERE i.medicionId = :medicionId")
    void deleteByMedicionId(@Param("medicionId") Integer medicionId);
}