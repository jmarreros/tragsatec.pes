package tragsatec.pes.persistence.repository.calculo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tragsatec.pes.dto.calculo.IndicadorDataProjection;
import tragsatec.pes.persistence.entity.calculo.IndicadorSequiaEntity;

import java.math.BigDecimal;
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
            "    SUM(prep1) AS suma_ultimos_n_prep1 " +
            "FROM " +
            "    RankedIndicadores " +
            "WHERE " +
            "    rn <= :numMeses " + // Usar el parámetro aquí
            "GROUP BY " +
            "    estacion_id", nativeQuery = true)
    List<Object[]> sumLastNPrep1ForEachEstacion(@Param("numMeses") Integer numMeses); // Añadir parámetro al método

    @Query(value = "SELECT anio, mes, prep1 AS dato, ie_b1 AS indicador FROM indicador_sequia WHERE estacion_id = :estacionId ORDER BY anio, mes", nativeQuery = true)
    List<IndicadorDataProjection> getAllDataIndicadorAnioMesPrep1(@Param("estacionId") Integer estacionId);

    @Query(value = "SELECT anio, mes, prep3 AS dato, ie_b3 AS indicador FROM indicador_sequia WHERE estacion_id = :estacionId ORDER BY anio, mes", nativeQuery = true)
    List<IndicadorDataProjection> getAllDataIndicadorAnioMesPrep3(@Param("estacionId") Integer estacionId);
}