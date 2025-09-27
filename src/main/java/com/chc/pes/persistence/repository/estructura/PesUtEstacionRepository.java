package com.chc.pes.persistence.repository.estructura;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.chc.pes.dto.general.EstacionProjection;
import com.chc.pes.persistence.entity.estructura.PesUtEstacionEntity;

import java.util.List;

@Repository
public interface PesUtEstacionRepository extends JpaRepository<PesUtEstacionEntity, Integer> {
    @Query("SELECT DISTINCT e.id AS id, e.codigo AS codigo " +
            "FROM PesUtEstacionEntity pute JOIN pute.estacion e " +
            "WHERE pute.pes.id = :pesId AND pute.tipo = :tipo")
    List<EstacionProjection> getAllEstacionesByPesId(@Param("pesId") Integer pesId,
                                                     @Param("tipo") Character tipo);

    List<PesUtEstacionEntity> findByPesIdAndTipo(Integer pesId, Character tipo);
}

