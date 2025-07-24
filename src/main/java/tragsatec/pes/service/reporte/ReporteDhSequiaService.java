package tragsatec.pes.service.reporte;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tragsatec.pes.dto.calculo.IndicadorDataProjection;
import tragsatec.pes.persistence.repository.calculo.IndicadorDhSequiaRepository;

import java.util.List;

@Service
public class ReporteDhSequiaService {

    private final IndicadorDhSequiaRepository indicadorDhSequiaRepository;

    @Autowired
    public ReporteDhSequiaService(IndicadorDhSequiaRepository indicadorDhSequiaRepository) {
        this.indicadorDhSequiaRepository = indicadorDhSequiaRepository;
    }

    @Transactional(readOnly = true)
    public List<IndicadorDataProjection> getAllDataIndicadorAnioMes(Integer demarcacionId, String tipoPrep) {
        if ("prep1".equalsIgnoreCase(tipoPrep)) {
            return indicadorDhSequiaRepository.getAllDataIndicadorAnioMesPrep1(demarcacionId);
        } else if ("prep3".equalsIgnoreCase(tipoPrep)) {
            return indicadorDhSequiaRepository.getAllDataIndicadorAnioMesPrep3(demarcacionId);
        } else {
            throw new IllegalArgumentException("El tipo de precipitación especificado no es válido: " + tipoPrep);
        }
    }
}