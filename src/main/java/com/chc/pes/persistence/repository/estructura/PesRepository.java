package com.chc.pes.persistence.repository.estructura;

import org.springframework.data.jpa.repository.JpaRepository;
// Ya no se necesita @Query para este método si se usa un nombre derivado
import org.springframework.stereotype.Repository;
import com.chc.pes.persistence.entity.estructura.PesEntity;

import java.util.Optional;

@Repository
public interface PesRepository extends JpaRepository<PesEntity, Integer> {

    // Método derivado para encontrar el Plan Especial de Sequía (PES) activo y aprobado
    Optional<PesEntity> findTopByActivoTrueAndAprobadoTrueOrderByCreatedAtDesc();
}