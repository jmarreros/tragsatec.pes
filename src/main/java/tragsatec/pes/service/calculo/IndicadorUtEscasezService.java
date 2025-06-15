package tragsatec.pes.service.calculo;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tragsatec.pes.persistence.entity.calculo.IndicadorUtEscasezEntity;
import tragsatec.pes.persistence.repository.calculo.IndicadorUtEscasezRepository;
import tragsatec.pes.persistence.repository.calculo.IndicadorUtSequiaRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class IndicadorUtEscasezService {
    private final IndicadorUtEscasezRepository indicadorUtEscasezRepository;

    @Transactional
    public void calcularYGuardarIndicadoresUtEscasez(Integer medicionId, Integer pesId) {
        // Eliminar registros existentes para el medicionId dado
        indicadorUtEscasezRepository.deleteByMedicionId(medicionId);

        // Insertar los nuevos registros calculados
        indicadorUtEscasezRepository.insertIndicadorUtEscasez(medicionId, pesId);
    }

}

