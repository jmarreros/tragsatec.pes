package tragsatec.pes.service.reporte;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tragsatec.pes.dto.calculo.IndicadorDataProjection;
import tragsatec.pes.dto.calculo.IndicadorDemarcacionFechaDataProjection;
import tragsatec.pes.dto.calculo.IndicadorFechaDataProjection;
import tragsatec.pes.dto.calculo.IndicadorUTFechaDataProjection;
import tragsatec.pes.persistence.repository.calculo.IndicadorUtEscasezRepository;
import tragsatec.pes.service.estructura.PesService;

import java.util.List;

@Service
public class ReporteUtEscasezService {

    private final IndicadorUtEscasezRepository indicadorUtEscasezRepository;
    private final PesService pesService;

    @Autowired
    public ReporteUtEscasezService(IndicadorUtEscasezRepository indicadorUtEscasezRepository, PesService pesService) {
        this.indicadorUtEscasezRepository = indicadorUtEscasezRepository;
        this.pesService = pesService;
    }

    @Transactional(readOnly = true)
    public List<IndicadorDataProjection> getAllDataIndicadorAnioMes(Integer utId) {
        return indicadorUtEscasezRepository.getAllDataIndicadorAnioMes(utId);
    }

    @Transactional(readOnly = true)
    public List<IndicadorFechaDataProjection> getAllDataFecha(Integer anio) {
        int startYear = anio;
        int endYear = anio + 1;
        int startMonth = 10;
        int endMonth = 9;
        return indicadorUtEscasezRepository.getAllDataFecha(startYear, startMonth, endYear, endMonth);
    }

    @Transactional(readOnly = true)
    public List<IndicadorDemarcacionFechaDataProjection> getAllDataFechaDemarcacion(Integer demarcacionId, Integer anio) {
        Integer pesId = pesService.findActiveAndApprovedPesId()
                .orElseThrow(() -> new RuntimeException("No se encontró ningún PES activo y aprobado."));

        int startYear = anio;
        int endYear = anio + 1;
        int startMonth = 10;
        int endMonth = 9;

        return indicadorUtEscasezRepository.getAllDataFechaDemarcacion(pesId, demarcacionId, startYear, startMonth, endYear, endMonth);
    }

    @Transactional(readOnly = true)
    public List<IndicadorUTFechaDataProjection> getTotalDataUTFecha(Integer utId, Integer anio) {
        int startYear = anio;
        int endYear = anio + 1;
        int startMonth = 10;
        int endMonth = 9;

        return indicadorUtEscasezRepository.getTotalDataUTFecha(utId, startYear, startMonth, endYear, endMonth);
    }
}