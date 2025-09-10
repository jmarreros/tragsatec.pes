package com.chc.pes.controller.reporte;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.chc.pes.dto.calculo.IndicadorDataProjection;
import com.chc.pes.dto.reporte.EstadisticasMensualesEscasezDTO;
import com.chc.pes.service.reporte.ReporteEstacionEscasezService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/reportes/estacion-escasez")
public class ReporteEstacionEscasezController {
    @Value("${report.max.year.escasez}")
    private Integer maxYear;

    private final ReporteEstacionEscasezService reporteEstacionEscasezService;

    @Autowired
    public ReporteEstacionEscasezController(ReporteEstacionEscasezService reporteEstacionEscasezService) {
        this.reporteEstacionEscasezService = reporteEstacionEscasezService;
    }

    @GetMapping("/{estacionId}")
    public ResponseEntity<List<IndicadorDataProjection>> getDataIndicadorAnioMes(
            @PathVariable Integer estacionId) {
        List<IndicadorDataProjection> datos = reporteEstacionEscasezService.getAllDataIndicadorAnioMes(estacionId);
        return ResponseEntity.ok(datos);
    }

    @GetMapping("/{estacionId}/estadisticas")
    public ResponseEntity<List<EstadisticasMensualesEscasezDTO>> getEstadisticasMensuales(
            @PathVariable Integer estacionId) {
        List<EstadisticasMensualesEscasezDTO> estadisticas = reporteEstacionEscasezService.getEstadisticasMensuales(estacionId);
        return ResponseEntity.ok(estadisticas);
    }

    @GetMapping("/estadisticas/getMaxYear")
    public ResponseEntity<Integer> getMaxYear() {
        return ResponseEntity.ok(maxYear);
    }
}