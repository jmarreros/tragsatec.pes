package tragsatec.pes.service.reporte;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tragsatec.pes.dto.calculo.IndicadorDataProjection;
import tragsatec.pes.persistence.repository.calculo.IndicadorDhEscasezRepository;

import java.util.List;

@Service
public class ReporteDhEscasezService {

    private final IndicadorDhEscasezRepository indicadorDhEscasezRepository;

    @Autowired
    public ReporteDhEscasezService(IndicadorDhEscasezRepository indicadorDhEscasezRepository) {
        this.indicadorDhEscasezRepository = indicadorDhEscasezRepository;
    }

    @Transactional(readOnly = true)
    public List<IndicadorDataProjection> getAllDataIndicadorAnioMes(Integer demarcacionId) {
        return indicadorDhEscasezRepository.getAllDataIndicadorAnioMes(demarcacionId);
    }
}