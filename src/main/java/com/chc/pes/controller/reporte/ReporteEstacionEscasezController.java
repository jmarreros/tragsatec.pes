package com.chc.pes.controller.reporte;

import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class ReporteEstacionEscasezController {
    private final ReporteEstacionEscasezService reporteEstacionEscasezService;

    @Autowired
    public ReporteEstacionEscasezController(ReporteEstacionEscasezService reporteEstacionEscasezService) {
        this.reporteEstacionEscasezService = reporteEstacionEscasezService;
    }

    @GetMapping("/{estacionId}")
    public ResponseEntity<List<IndicadorDataProjection>> getDataIndicadorAnioMes(
            @PathVariable Integer estacionId) {
        try {
            List<IndicadorDataProjection> datos = reporteEstacionEscasezService.getAllDataIndicadorAnioMes(estacionId);
            return ResponseEntity.ok(datos);
        } catch (Exception e) {

            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{estacionId}/estadisticas")
    public ResponseEntity<List<EstadisticasMensualesEscasezDTO>> getEstadisticasMensuales(
            @PathVariable Integer estacionId) {
        try{
            List<EstadisticasMensualesEscasezDTO> estadisticas = reporteEstacionEscasezService.getEstadisticasMensuales(estacionId);
            return ResponseEntity.ok(estadisticas);
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
}