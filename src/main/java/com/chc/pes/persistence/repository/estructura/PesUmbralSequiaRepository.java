package com.chc.pes.persistence.repository.estructura;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.chc.pes.persistence.entity.estructura.PesUmbralSequiaEntity;

import java.util.List;
import java.util.Optional;

@Repository
public interface PesUmbralSequiaRepository extends JpaRepository<PesUmbralSequiaEntity, Integer> {
    Optional<PesUmbralSequiaEntity> findByPesIdAndEstacionIdAndMes(Integer pesId, Integer estacionId, Byte mes);
    List<PesUmbralSequiaEntity> findByPesIdAndMes(Integer pesId, Byte mes);
}
