package com.chc.pes.persistence.repository.calculo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.chc.pes.dto.calculo.IndicadorDataProjection;
import com.chc.pes.persistence.entity.calculo.IndicadorEscasezEntity;

import java.util.List;

@Repository
public interface IndicadorEscasezRepository extends JpaRepository<IndicadorEscasezEntity, Long> {
    void deleteByMedicionId(Integer medicionId);

    @Query(value = "SELECT anio, mes, dato, ie AS indicador FROM indicador_escasez WHERE estacion_id = :estacionId AND anio <= :maxYear ORDER BY anio, mes", nativeQuery = true)
    List<IndicadorDataProjection> getAllDataIndicadorAnioMes(@Param("estacionId") Integer estacionId, @Param("maxYear") Integer maxYear);
}