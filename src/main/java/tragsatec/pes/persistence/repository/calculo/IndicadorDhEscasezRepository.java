package tragsatec.pes.persistence.repository.calculo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tragsatec.pes.persistence.entity.calculo.IndicadorDhEscasezEntity;

@Repository
public interface IndicadorDhEscasezRepository extends JpaRepository<IndicadorDhEscasezEntity, Long> {

    @Modifying
    @Query(value = "INSERT INTO indicador_dh_escasez(" +
            "    medicion_id, " +
            "    demarcacion_id, " +
            "    dato, " +
            "    ie, " +
            "    anio, " +
            "    mes" +
            ") " +
            "SELECT " +
            "    iute.medicion_id, " +
            "    ut.demarcacion_id, " +
            "    SUM(iute.dato * pdut.coeficiente / 100.0), " +
            "    SUM(iute.ie * pdut.coeficiente / 100.0), " +
            "    iute.anio, " +
            "    iute.mes " +
            "FROM indicador_ut_escasez iute " +
            "INNER JOIN unidad_territorial ut ON iute.unidad_territorial_id = ut.id " +
            "INNER JOIN pes_demarcacion_ut pdut ON pdut.unidad_territorial_id = ut.id " + // Asumiendo que esta condición de JOIN es correcta
            "WHERE iute.medicion_id = :medicionId " +
            "  AND pdut.pes_id = :pesId " +
            "GROUP BY ut.demarcacion_id, iute.medicion_id, iute.anio, iute.mes", nativeQuery = true)
    void insertIndicadorDhEscasez(
            @Param("medicionId") Integer medicionId,
            @Param("pesId") Integer pesId
    );

    @Modifying
    @Query("DELETE FROM IndicadorDhEscasezEntity i WHERE i.medicionId = :medicionId")
    void deleteByMedicionId(@Param("medicionId") Integer medicionId);
}