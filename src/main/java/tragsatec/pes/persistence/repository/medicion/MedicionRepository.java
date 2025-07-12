package tragsatec.pes.persistence.repository.medicion;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tragsatec.pes.dto.medicion.MedicionHistorialProjection;
import tragsatec.pes.persistence.entity.medicion.MedicionEntity;

import java.util.List;

@Repository
public interface MedicionRepository extends JpaRepository<MedicionEntity, Integer> {
    List<MedicionEntity> findByPesIdAndTipoAndAnioAndMesAndEliminadoFalse(Integer pesId, Character tipo, Short anio, Byte mes);

    @Query(value = "SELECT TOP 1 m.* FROM medicion m WHERE m.eliminado = 0 AND m.procesado = 0 AND m.tipo = :tipo ORDER BY m.anio ASC, m.mes ASC", nativeQuery = true)
    MedicionEntity findFirstNotProcessedMedicionByTipo(@Param("tipo") Character tipo);

    @Query(value = "SELECT TOP 1 m.* FROM medicion m WHERE m.eliminado = 0 AND m.procesado = 1 AND m.tipo = :tipo ORDER BY m.anio DESC, m.mes DESC", nativeQuery = true)
    MedicionEntity findLastProcessedMedicionByTipo(@Param("tipo") Character tipo);

    @Modifying
    @Query("UPDATE MedicionEntity m SET m.procesado = :procesado WHERE m.id = :id")
    void actualizarEstadoProcesado(@Param("id") Integer id, @Param("procesado") Boolean procesado);

    @Query(value = "SELECT m.id," +
            "m.anio as anio, m.mes as mes, m.procesado as procesado, m.eliminado as eliminado, " +
            "am.id as file_id, am.file_name as fileName," +
            "m.created_by as createdBy, m.created_at as createdAt " +
            "FROM medicion m " +
            "LEFT JOIN archivo_medicion am ON m.id = am.medicion_id " +
            "WHERE m.anio = :anio AND m.tipo = :tipo AND m.eliminado = 0 " +
            "ORDER BY m.anio DESC, m.mes DESC",
            nativeQuery = true)
    List<MedicionHistorialProjection> findHistorialByAnioAndTipo(
            @Param("anio") Short anio,
            @Param("tipo") Character tipo);
}

