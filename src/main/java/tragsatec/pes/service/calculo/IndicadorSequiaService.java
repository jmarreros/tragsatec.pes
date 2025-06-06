package tragsatec.pes.service.calculo;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tragsatec.pes.persistence.entity.calculo.IndicadorSequiaEntity;
import tragsatec.pes.persistence.repository.calculo.IndicadorSequiaRepository;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class IndicadorSequiaService {

    private final IndicadorSequiaRepository repository;

    @Transactional
    public IndicadorSequiaEntity calcularIndicadorSequia(Integer estacionId, Short anio, Byte mes) {
        // Crear nueva entidad para almacenar resultados
        IndicadorSequiaEntity indicador = new IndicadorSequiaEntity();
        indicador.setAnio(anio);
        indicador.setMes(mes);
        indicador.setEstacionId(estacionId);

        return repository.save(indicador);
    }

}
