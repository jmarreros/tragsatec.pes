package tragsatec.pes.persistence.repository.estructura;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import tragsatec.pes.persistence.entity.estructura.PesEntity;

import java.util.Optional;

@Repository
public interface PesRepository extends JpaRepository<PesEntity, Integer> {

    // Corregido para SQL Server
    @Query(value = "SELECT TOP 1 id FROM pes WHERE activo = 1 AND aprobado = 1 ORDER BY created_at DESC", nativeQuery = true)
    Optional<Integer> findActiveAndApprovedPesId();
}