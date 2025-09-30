package com.chc.pes.persistence.repository.estructura;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.chc.pes.persistence.entity.estructura.PesDemarcacionUtEntity;

import java.util.List;

@Repository
public interface PesDemarcacionUtRepository extends JpaRepository<PesDemarcacionUtEntity, Integer> {
    List<PesDemarcacionUtEntity> findByPesIdAndTipo(Integer pesId, Character tipo);
}

