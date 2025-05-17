package tragsatec.pes.service.general;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tragsatec.pes.persistence.entity.general.EstacionEntity;
import tragsatec.pes.persistence.repository.general.EstacionRepository;

import java.util.List;
import java.util.Optional;

@Service
public class EstacionService {
    private final EstacionRepository estacionRepository;

    @Autowired
    public EstacionService(EstacionRepository estacionRepository) {
        this.estacionRepository = estacionRepository;
    }

    public List<EstacionEntity> findAll() {
        return (List<EstacionEntity>) estacionRepository.findAll();
    }

    public Optional<EstacionEntity> findById(String id) {
        return estacionRepository.findById(id);
    }

    public EstacionEntity save(EstacionEntity entity) {
        return estacionRepository.save(entity);
    }
}

