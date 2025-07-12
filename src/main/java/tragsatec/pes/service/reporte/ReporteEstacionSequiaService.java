package tragsatec.pes.service.reporte;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tragsatec.pes.dto.calculo.IndicadorDataProjection;
import tragsatec.pes.persistence.repository.calculo.IndicadorSequiaRepository;

import java.util.List;

@Service
public class ReporteEstacionSequiaService {

    private final IndicadorSequiaRepository indicadorSequiaRepository;

    @Autowired
    public ReporteEstacionSequiaService(IndicadorSequiaRepository indicadorSequiaRepository) {
        this.indicadorSequiaRepository = indicadorSequiaRepository;
    }

    @Transactional(readOnly = true)
    public List<IndicadorDataProjection> getAllDataIndicadorAnioMes(Integer estacionId, String tipoPrep) {
        if ("Prep1".equalsIgnoreCase(tipoPrep)) {
            return indicadorSequiaRepository.getAllDataIndicadorAnioMesPrep1(estacionId);
        } else if ("Prep3".equalsIgnoreCase(tipoPrep)) {
            return indicadorSequiaRepository.getAllDataIndicadorAnioMesPrep3(estacionId);
        } else {
            throw new IllegalArgumentException("El tipo de preparación especificado no es válido: " + tipoPrep);
        }
    }
}