package tragsatec.pes.persistence.repository.medicion;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tragsatec.pes.persistence.entity.medicion.MedicionEntity;

import java.util.List;

@Repository
public interface MedicionRepository extends JpaRepository<MedicionEntity, Integer> {
    List<MedicionEntity> findByPesIdAndTipoAndAnioAndMesAndEliminadoFalse(Integer pesId, Character tipo, Short anio, Byte mes);

    @Query(value = "SELECT TOP 1 m.* FROM medicion m WHERE m.eliminado = 0 AND m.procesado = 0 AND m.tipo = :tipo ORDER BY m.anio DESC, m.mes DESC", nativeQuery = true)
    MedicionEntity findFirstNotProcessedMedicionByTipo(@Param("tipo") Character tipo);

    @Modifying
    @Query("UPDATE MedicionEntity m SET m.procesado = :procesado WHERE m.id = :id")
    void actualizarEstadoProcesado(@Param("id") Integer id, @Param("procesado") Boolean procesado);
}

