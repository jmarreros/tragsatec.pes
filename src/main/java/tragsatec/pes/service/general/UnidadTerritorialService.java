package tragsatec.pes.service.general;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tragsatec.pes.persistence.entity.general.UnidadTerritorialEntity;
import tragsatec.pes.persistence.repository.general.UnidadTerritorialRepository;

import java.util.List;
import java.util.Optional;

@Service
public class UnidadTerritorialService {
    private final UnidadTerritorialRepository unidadTerritorialRepository;

    @Autowired
    public UnidadTerritorialService(UnidadTerritorialRepository unidadTerritorialRepository) {
        this.unidadTerritorialRepository = unidadTerritorialRepository;
    }

    public List<UnidadTerritorialEntity> findAll() {
        return (List<UnidadTerritorialEntity>) unidadTerritorialRepository.findAll();
    }

    public Optional<UnidadTerritorialEntity> findById(String id) {
        return unidadTerritorialRepository.findById(id);
    }

    public UnidadTerritorialEntity save(UnidadTerritorialEntity entity) {
        return unidadTerritorialRepository.save(entity);
    }
}

