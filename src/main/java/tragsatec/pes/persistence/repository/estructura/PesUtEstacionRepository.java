package tragsatec.pes.persistence.repository.estructura;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tragsatec.pes.persistence.entity.estructura.PesUtEstacionEntity;

@Repository
public interface PesUtEstacionRepository extends JpaRepository<PesUtEstacionEntity, Integer> {
}

