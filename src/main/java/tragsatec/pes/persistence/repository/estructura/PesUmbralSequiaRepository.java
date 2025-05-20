package tragsatec.pes.persistence.repository.estructura;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tragsatec.pes.persistence.entity.estructura.PesUmbralSequiaEntity;

@Repository
public interface PesUmbralSequiaRepository extends JpaRepository<PesUmbralSequiaEntity, Integer> {
}

