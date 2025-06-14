package tragsatec.pes.service.calculo; // O el paquete donde esté tu servicio

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tragsatec.pes.persistence.repository.calculo.IndicadorUtSequiaRepository;

@Service
@RequiredArgsConstructor
public class IndicadorUtSequiaService {

    private final IndicadorUtSequiaRepository indicadorUtSequiaRepository;

    @Transactional
    public void calcularYGuardarIndicadoresUtSequia(Integer medicionId, Integer pesId) {
        // Eliminar registros existentes para el medicionId dado
        indicadorUtSequiaRepository.deleteByMedicionId(medicionId);

        // Insertar los nuevos registros calculados
        indicadorUtSequiaRepository.insertIndicadorUtSequia(medicionId, pesId);
    }
}