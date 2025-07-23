package tragsatec.pes.service.reporte;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tragsatec.pes.dto.calculo.IndicadorDataProjection;
import tragsatec.pes.persistence.repository.calculo.IndicadorUtSequiaRepository;

import java.util.List;

@Service
public class ReporteUtSequiaService {

    private final IndicadorUtSequiaRepository indicadorUtSequiaRepository;

    @Autowired
    public ReporteUtSequiaService(IndicadorUtSequiaRepository indicadorUtSequiaRepository) {
        this.indicadorUtSequiaRepository = indicadorUtSequiaRepository;
    }

    @Transactional(readOnly = true)
    public List<IndicadorDataProjection> getAllDataIndicadorAnioMes(Integer utId, String tipoPrep) {
        if ("prep1".equalsIgnoreCase(tipoPrep)) {
            return indicadorUtSequiaRepository.getAllDataIndicadorAnioMesPrep1(utId);
        } else if ("prep3".equalsIgnoreCase(tipoPrep)) {
            return indicadorUtSequiaRepository.getAllDataIndicadorAnioMesPrep3(utId);
        } else {
            throw new IllegalArgumentException("El tipo de precipitación especificado no es válido: " + tipoPrep);
        }
    }
}
