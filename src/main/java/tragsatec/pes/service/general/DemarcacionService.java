package tragsatec.pes.service.general;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tragsatec.pes.persistence.entity.general.DemarcacionEntity;
import tragsatec.pes.persistence.repository.general.DemarcacionRepository;

import java.util.List;
import java.util.Optional;

@Service
public class DemarcacionService {
    private final DemarcacionRepository demarcacionRepository;

    @Autowired
    public DemarcacionService(DemarcacionRepository demarcacionRepository) {
        this.demarcacionRepository = demarcacionRepository;
    }

    public List<DemarcacionEntity> findAll() {
        return (List<DemarcacionEntity>) demarcacionRepository.findAll();
    }

    public Optional<DemarcacionEntity> findById(Integer id) {
        return demarcacionRepository.findById(id);
    }

    public DemarcacionEntity save(DemarcacionEntity entity) {
        return demarcacionRepository.save(entity);
    }
}

