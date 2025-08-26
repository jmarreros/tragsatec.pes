package com.chc.pes.persistence.repository.estructura;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.chc.pes.persistence.entity.estructura.PesDemarcacionUtEntity;

@Repository
public interface PesDemarcacionUtRepository extends JpaRepository<PesDemarcacionUtEntity, Integer> {
}

