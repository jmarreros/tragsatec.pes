package tragsatec.pes.service.reporte;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tragsatec.pes.dto.calculo.IndicadorDataProjection;
import tragsatec.pes.persistence.repository.calculo.IndicadorUtEscasezRepository;

import java.util.List;

@Service
public class ReporteUtEscasezService {

    private final IndicadorUtEscasezRepository indicadorUtEscasezRepository;

    @Autowired
    public ReporteUtEscasezService(IndicadorUtEscasezRepository indicadorUtEscasezRepository) {
        this.indicadorUtEscasezRepository = indicadorUtEscasezRepository;
    }

    @Transactional(readOnly = true)
    public List<IndicadorDataProjection> getAllDataIndicadorAnioMes(Integer utId) {
        return indicadorUtEscasezRepository.getAllDataIndicadorAnioMes(utId);
    }
}