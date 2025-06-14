package tragsatec.pes.service.calculo;

import org.springframework.stereotype.Service;
import tragsatec.pes.persistence.entity.calculo.IndicadorUtEscasezEntity;
import tragsatec.pes.persistence.repository.calculo.IndicadorUtEscasezRepository;

import java.util.List;

@Service
public class IndicadorUtEscasezService {
    private final IndicadorUtEscasezRepository repository;

    public IndicadorUtEscasezService(IndicadorUtEscasezRepository repository) {
        this.repository = repository;
    }

    public List<IndicadorUtEscasezEntity> findAll() {
        return repository.findAll();
    }

    public IndicadorUtEscasezEntity save(IndicadorUtEscasezEntity entity) {
        return repository.save(entity);
    }

    public void deleteById(Long id) {
        repository.deleteById(id);
    }
}

