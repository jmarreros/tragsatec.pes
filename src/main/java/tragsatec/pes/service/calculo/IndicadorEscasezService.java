package tragsatec.pes.service.calculo;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tragsatec.pes.persistence.entity.calculo.IndicadorEscasezEntity;
import tragsatec.pes.persistence.repository.calculo.IndicadorEscasezRepository;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class IndicadorEscasezService {

    private final IndicadorEscasezRepository repository;

    @Transactional
    public IndicadorEscasezEntity calcularIndicadorEscasez(Integer estacionId, Short anio, Byte mes) {
        // Crear nueva entidad para almacenar resultados
        IndicadorEscasezEntity indicador = new IndicadorEscasezEntity();
        indicador.setAnio(anio);
        indicador.setMes(mes);
        indicador.setEstacionId(estacionId);

        return repository.save(indicador);

    }
}
