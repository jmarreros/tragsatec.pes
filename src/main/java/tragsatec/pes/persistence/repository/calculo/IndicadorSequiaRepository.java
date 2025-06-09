package tragsatec.pes.persistence.repository.calculo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tragsatec.pes.persistence.entity.calculo.IndicadorSequiaEntity;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface IndicadorSequiaRepository extends JpaRepository<IndicadorSequiaEntity, Long> {
    void deleteByMedicionId(Integer medicionId);

    List<IndicadorSequiaEntity> findByMedicionId(Integer medicionId);

    // Suma de prep1 de los últimos N meses para una estación específica
    @Query(value = "SELECT SUM(prep1) FROM (SELECT TOP :numMeses prep1 FROM indicador_sequia " +
            "WHERE estacion_id = :estacionId ORDER BY anio DESC, mes DESC) AS subquery",
            nativeQuery = true)
    BigDecimal sumLastNPrep1ByEstacionId(
            @Param("estacionId") Integer estacionId,
            @Param("numMeses") Integer numMeses);


}
