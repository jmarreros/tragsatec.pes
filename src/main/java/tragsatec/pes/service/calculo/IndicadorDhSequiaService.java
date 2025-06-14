package tragsatec.pes.service.calculo; // O el paquete donde desees ubicar tu servicio

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tragsatec.pes.persistence.repository.calculo.IndicadorDhSequiaRepository;

@Service
@RequiredArgsConstructor
public class IndicadorDhSequiaService {

    private final IndicadorDhSequiaRepository indicadorDhSequiaRepository;

    @Transactional
    public void calcularYGuardarIndicadoresDhSequia(Integer medicionId, Integer pesId) {
        // Eliminar registros existentes para el medicionId dado
        indicadorDhSequiaRepository.deleteByMedicionId(medicionId);

        // Insertar los nuevos registros calculados
        indicadorDhSequiaRepository.insertIndicadorDhSequia(medicionId, pesId);
    }
}