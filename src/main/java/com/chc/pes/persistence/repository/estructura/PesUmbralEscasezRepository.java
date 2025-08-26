package com.chc.pes.persistence.repository.estructura;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.chc.pes.dto.estructura.UmbralEscasezDataProjection;
import com.chc.pes.dto.estructura.UmbralEscasezMesDataProjection;
import com.chc.pes.persistence.entity.estructura.PesUmbralEscasezEntity;

import java.util.List;
import java.util.Optional;

@Repository
public interface PesUmbralEscasezRepository extends JpaRepository<PesUmbralEscasezEntity, Integer> {
    /**
     * Obtiene los datos brutos de umbrales para un PES y un número de mes específico.
     * La concatenación 'escenario + estadistico' es para SQL Server.
     *
     * @param pesId El ID del PES.
     * @param mesNumero El número del mes (1-12) para el cual obtener el valor.
     * @return Una lista de objetos UmbralEscasezRawData.
     */
    @Query(value = "SELECT " +
                   "    pue.estacion_id AS estacionId, " +
                   "    pue.param AS factor, " +
                   "    CASE :mesNumero " +
                   "        WHEN 1 THEN pue.mes_1 " +
                   "        WHEN 2 THEN pue.mes_2 " +
                   "        WHEN 3 THEN pue.mes_3 " +
                   "        WHEN 4 THEN pue.mes_4 " +
                   "        WHEN 5 THEN pue.mes_5 " +
                   "        WHEN 6 THEN pue.mes_6 " +
                   "        WHEN 7 THEN pue.mes_7 " +
                   "        WHEN 8 THEN pue.mes_8 " +
                   "        WHEN 9 THEN pue.mes_9 " +
                   "        WHEN 10 THEN pue.mes_10 " +
                   "        WHEN 11 THEN pue.mes_11 " +
                   "        WHEN 12 THEN pue.mes_12 " +
                   "        ELSE 0 " +
                   "    END AS valorMes " +
                   "FROM pes_umbral_escasez pue " +
                   "WHERE pue.pes_id = :pesId", nativeQuery = true)
    List<UmbralEscasezMesDataProjection> findRawUmbralesByPesIdAndMes(@Param("pesId") Integer pesId, @Param("mesNumero") Byte mesNumero);


    /**
     * Obtiene los umbrales de escasez para una estación y un PES específico.
     *
     * @param estacionId El ID de la estación.
     * @param pesId      El ID del PES.
     * @return Una lista de proyecciones con los datos de umbrales.
     */
    @Query(value = "SELECT " +
            "    param, mes_1, mes_2, mes_3, mes_4, mes_5, mes_6, " +
            "    mes_7, mes_8, mes_9, mes_10, mes_11, mes_12 " +
            "FROM pes_umbral_escasez " +
            "WHERE estacion_id = :estacionId AND pes_id = :pesId", nativeQuery = true)
    List<UmbralEscasezDataProjection> findUmbralesByEstacionIdAndPesId(@Param("estacionId") Integer estacionId, @Param("pesId") Optional<Integer> pesId);
}