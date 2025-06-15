package tragsatec.pes.service.calculo;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tragsatec.pes.persistence.repository.calculo.IndicadorDhEscasezRepository;

@Service
@RequiredArgsConstructor
public class IndicadorDhEscasezService {
    private final IndicadorDhEscasezRepository indicadorDhEscasezRepository;

    @Transactional
    public void calcularYGuardarIndicadoresDhEscasez(Integer medicionId, Integer pesId) {
        // Eliminar registros existentes para el medicionId dado
        indicadorDhEscasezRepository.deleteByMedicionId(medicionId);

        // Insertar los nuevos registros calculados
        indicadorDhEscasezRepository.insertIndicadorDhEscasez(medicionId, pesId);
    }
}

