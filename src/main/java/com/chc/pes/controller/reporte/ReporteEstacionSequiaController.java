package com.chc.pes.controller.reporte;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.chc.pes.dto.calculo.IndicadorDataProjection;
import com.chc.pes.dto.reporte.EstadisticasMensualesSequiaDTO;
import com.chc.pes.service.reporte.ReporteEstacionSequiaService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/reportes/estacion-sequia")
@Slf4j
public class ReporteEstacionSequiaController {
    private final ReporteEstacionSequiaService reporteEstacionSequiaService;

    @Autowired
    public ReporteEstacionSequiaController(ReporteEstacionSequiaService reporteEstacionSequiaService) {
        this.reporteEstacionSequiaService = reporteEstacionSequiaService;
    }

    @GetMapping("/{estacionId}")
    public ResponseEntity<?> getDataIndicadorAnioMes(
            @PathVariable Integer estacionId,
            @RequestParam String tipoPrep) {
        try {
            List<IndicadorDataProjection> datos = reporteEstacionSequiaService.getAllDataIndicadorAnioMes(estacionId, tipoPrep);
            return ResponseEntity.ok(datos);
        } catch (IllegalArgumentException e) {
            log.error(e.getMessage());
            return ResponseEntity.badRequest().body("Error al obtener los indicadores");
        }
    }

    @GetMapping("/{estacionId}/estadisticas")
    public ResponseEntity<?> getEstadisticasMensuales(
            @PathVariable Integer estacionId,
            @RequestParam String tipoPrep) {
        try {
            List<EstadisticasMensualesSequiaDTO> estadisticas = reporteEstacionSequiaService.getEstadisticasMensuales(estacionId, tipoPrep);
            return ResponseEntity.ok(estadisticas);
        } catch (IllegalArgumentException e) {
            log.error(e.getMessage());
            return ResponseEntity.badRequest().body("Error al obtener las estadísticas mensuales");
        }
    }
}