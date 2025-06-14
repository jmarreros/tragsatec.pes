package tragsatec.pes.service.calculo;

import org.springframework.stereotype.Service;
import tragsatec.pes.persistence.entity.calculo.indicadorDhEscasezEntity;
import tragsatec.pes.persistence.repository.calculo.IndicadorDhEscasezRepository;

import java.util.List;

@Service
public class IndicadorDhEscasezService {
    private final IndicadorDhEscasezRepository repository;

    public IndicadorDhEscasezService(IndicadorDhEscasezRepository repository) {
        this.repository = repository;
    }

    public List<indicadorDhEscasezEntity> findAll() {
        return repository.findAll();
    }

    public indicadorDhEscasezEntity save(indicadorDhEscasezEntity entity) {
        return repository.save(entity);
    }

    public void deleteById(Long id) {
        repository.deleteById(id);
    }
}

