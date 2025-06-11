package tragsatec.pes.persistence.repository.estructura;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tragsatec.pes.dto.estructura.UmbralEscasezRawDataDTO;
import tragsatec.pes.persistence.entity.estructura.PesUmbralEscasezEntity;

import java.util.List;
import java.util.Map;

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
                   "    pue.escenario + pue.estadistico AS factor, " +
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
    List<UmbralEscasezRawDataDTO> findRawUmbralesByPesIdAndMes(@Param("pesId") Integer pesId, @Param("mesNumero") Integer mesNumero);
}