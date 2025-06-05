package tragsatec.pes.service.calculo;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tragsatec.pes.persistence.entity.calculo.IndicadorEscasezEntity;
import tragsatec.pes.persistence.repository.calculo.IndicadorEscasezRepository;

@Service
@RequiredArgsConstructor
public class IndicadorEscasezService {

    private final IndicadorEscasezRepository repository;

    // Basic CRUD methods can be added here if needed
}

