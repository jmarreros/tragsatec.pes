package tragsatec.pes.service.calculo;

import org.springframework.stereotype.Service;
import tragsatec.pes.persistence.entity.calculo.IndicadorDhSequiaEntity;
import tragsatec.pes.persistence.repository.calculo.IndicadorDhSequiaRepository;

import java.util.List;

@Service
public class IndicadorDhSequiaService {
    private final IndicadorDhSequiaRepository repository;

    public IndicadorDhSequiaService(IndicadorDhSequiaRepository repository) {
        this.repository = repository;
    }

    public List<IndicadorDhSequiaEntity> findAll() {
        return repository.findAll();
    }

    public IndicadorDhSequiaEntity save(IndicadorDhSequiaEntity entity) {
        return repository.save(entity);
    }

    public void deleteById(Long id) {
        repository.deleteById(id);
    }
}

