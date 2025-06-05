package tragsatec.pes.service.calculo;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tragsatec.pes.persistence.entity.calculo.IndicadorSequiaEntity;
import tragsatec.pes.persistence.repository.calculo.IndicadorSequiaRepository;

@Service
@RequiredArgsConstructor
public class IndicadorSequiaService {

    private final IndicadorSequiaRepository repository;

    // Basic CRUD methods can be added here if needed
}
