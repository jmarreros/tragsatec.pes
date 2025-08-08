package tragsatec.pes.service.reporte;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tragsatec.pes.dto.calculo.IndicadorDataProjection;
import tragsatec.pes.dto.calculo.IndicadorDemarcacionFechaDataProjection;
import tragsatec.pes.dto.calculo.IndicadorFechaDataProjection;
import tragsatec.pes.dto.calculo.IndicadorUTFechaDataProjection;
import tragsatec.pes.persistence.repository.calculo.IndicadorUtSequiaRepository;
import tragsatec.pes.service.estructura.PesService;

import java.util.List;

@Service
public class ReporteUtSequiaService {

    private final IndicadorUtSequiaRepository indicadorUtSequiaRepository;
    private final PesService pesService;

    @Autowired
    public ReporteUtSequiaService(IndicadorUtSequiaRepository indicadorUtSequiaRepository, PesService pesService) {
        this.indicadorUtSequiaRepository = indicadorUtSequiaRepository;
        this.pesService = pesService;
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

    @Transactional(readOnly = true)
    public List<IndicadorFechaDataProjection> getAllDataFecha(Integer anio, String tipoPrep) {
        int startYear = anio;
        int endYear = anio + 1;
        int startMonth = 10;
        int endMonth = 9;

        if ("prep1".equalsIgnoreCase(tipoPrep)) {
            return indicadorUtSequiaRepository.getAllDataFechaPrep1(startYear, startMonth, endYear, endMonth);
        } else if ("prep3".equalsIgnoreCase(tipoPrep)) {
            return indicadorUtSequiaRepository.getAllDataFechaPrep3(startYear, startMonth, endYear, endMonth);
        } else {
            throw new IllegalArgumentException("El tipo de precipitación especificado no es válido: " + tipoPrep);
        }
    }

    @Transactional(readOnly = true)
    public List<IndicadorDemarcacionFechaDataProjection> getAllDataFechaDemarcacion(Integer demarcacionId, Integer anio) {
        Integer pesId = pesService.findActiveAndApprovedPesId()
                .orElseThrow(() -> new RuntimeException("No se encontró ningún PES activo y aprobado."));

        int startYear = anio;
        int endYear = anio + 1;
        int startMonth = 10;
        int endMonth = 9;

        return indicadorUtSequiaRepository.getAllDataFechaDemarcacion(pesId, demarcacionId, startYear, startMonth, endYear, endMonth);
    }

    @Transactional(readOnly = true)
    public List<IndicadorUTFechaDataProjection> getTotalDataUTFecha(Integer utId, Integer anio) {
        int startYear = anio;
        int endYear = anio + 1;
        int startMonth = 10;
        int endMonth = 9;

        return indicadorUtSequiaRepository.getTotalDataUTFecha(utId, startYear, startMonth, endYear, endMonth);
    }

    @Transactional(readOnly = true)
    public List<IndicadorUTFechaDataProjection> getUTEstacionFecha(Integer utId, Integer anio) {
        int startYear = anio;
        int endYear = anio + 1;
        int startMonth = 10;
        int endMonth = 9;

        return indicadorUtSequiaRepository.getUTEstacionFecha(utId, startYear, startMonth, endYear, endMonth);
    }
}