package com.chc.pes.service.reporte;

import com.chc.pes.dto.calculo.IndicadorDemarcacionFechaDataProjection;
import com.chc.pes.dto.calculo.IndicadorUTFechaDataProjection;
import com.chc.pes.dto.general.DemarcacionProjection;
import com.chc.pes.dto.general.UnidadTerritorialProjection;
import com.chc.pes.persistence.repository.calculo.IndicadorUtSequiaRepository;
import com.chc.pes.persistence.repository.estructura.PesUtEstacionRepository;
import com.chc.pes.persistence.repository.general.UnidadTerritorialRepository;
import com.chc.pes.service.general.DemarcacionService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReporteWordUtSequiaService {
    @Value("${file.report-dir}")
    private String reportDir;

    @Value("${file.temporal-dir}")
    private String temporalDir;

    private final DemarcacionService demarcacionService;
    private final ReporteUtSequiaService reporteUtSequiaService;
    private final IndicadorUtSequiaRepository indicadorUtSequiaRepository;
    private final UnidadTerritorialRepository unidadTerritorialRepository;
    private final PesUtEstacionRepository pesUtEstacionRepository;

    public ReporteWordUtSequiaService(DemarcacionService demarcacionService, ReporteUtSequiaService reporteUtSequiaService, IndicadorUtSequiaRepository indicadorUtSequiaRepository, UnidadTerritorialRepository unidadTerritorialRepository, PesUtEstacionRepository pesUtEstacionRepository) {
        this.demarcacionService = demarcacionService;
        this.reporteUtSequiaService = reporteUtSequiaService;
        this.indicadorUtSequiaRepository = indicadorUtSequiaRepository;
        this.unidadTerritorialRepository = unidadTerritorialRepository;
        this.pesUtEstacionRepository = pesUtEstacionRepository;
    }

    private List<IndicadorUTFechaDataProjection> obtenerDatosUTFecha(Integer utId, Integer anio) {
        return reporteUtSequiaService.getUTEstacionFecha(utId, anio);
    }

    private List<IndicadorUTFechaDataProjection> obtenerTotalesUTFecha(Integer utId, Integer anio) {
        return reporteUtSequiaService.getTotalDataUTFecha(utId, anio);
    }

    private List<UnidadTerritorialProjection> getUTsPorDemarcacionEscasez(Integer demarcacionId) {
        return unidadTerritorialRepository.findUnidadesTerritorialesByTipoDemarcacionAndPes('S', demarcacionId);
    }

    private DemarcacionProjection getDemarcacionInfo(String ubicacion) {
        List<DemarcacionProjection> demarcacionesEscasez = demarcacionService.findDemarcacionesByTipo('S');
        return demarcacionesEscasez.stream()
                .filter(d -> d.getNombre().toLowerCase().contains(ubicacion))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No se encontró la demarcación de tipo: " + ubicacion));
    }

    private List<IndicadorDemarcacionFechaDataProjection> datosDemarcacionUTEscasez(Integer anio, String tipo) {
        List<DemarcacionProjection> demarcacionesEscasez = demarcacionService.findDemarcacionesByTipo('S');
        // Buscar en la lista de demarcaciones en el campo de nombre que tenga el texto: "oriental" u "occidental" según el tipo
        DemarcacionProjection demarcacionTipo = demarcacionesEscasez.stream()
                .filter(d -> d.getNombre().toLowerCase().contains(tipo))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No se encontró la demarcación de tipo: " + tipo));
        Integer demarcacionId = demarcacionTipo.getId();

        return reporteUtSequiaService.getAllDataFechaDemarcacion(demarcacionId, anio, "prep1");
    }
}
