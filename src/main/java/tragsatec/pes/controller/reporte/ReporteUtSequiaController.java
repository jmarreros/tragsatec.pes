package tragsatec.pes.controller.reporte;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tragsatec.pes.dto.calculo.IndicadorDataProjection;
import tragsatec.pes.dto.calculo.IndicadorDemarcacionFechaDataProjection;
import tragsatec.pes.dto.calculo.IndicadorFechaDataProjection;
import tragsatec.pes.service.reporte.ReporteUtSequiaService;

import java.util.List;

@RestController
@RequestMapping("/reportes/ut-sequia")
public class ReporteUtSequiaController {

    private final ReporteUtSequiaService reporteUtSequiaService;

    @Autowired
    public ReporteUtSequiaController(ReporteUtSequiaService reporteUtSequiaService) {
        this.reporteUtSequiaService = reporteUtSequiaService;
    }

    @GetMapping("/{utId}")
    public ResponseEntity<?> getDataIndicadorAnioMes(
            @PathVariable Integer utId,
            @RequestParam String tipoPrep) {
        try {
            List<IndicadorDataProjection> datos = reporteUtSequiaService.getAllDataIndicadorAnioMes(utId, tipoPrep);
            return ResponseEntity.ok(datos);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/fecha/{anio}")
    public ResponseEntity<?> getDataFecha(
            @PathVariable Integer anio,
            @RequestParam String tipoPrep) {
        try {
            List<IndicadorFechaDataProjection> datos = reporteUtSequiaService.getAllDataFecha(anio, tipoPrep);
            return ResponseEntity.ok(datos);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Get all Unidad Territorial data for a specific demarcation and year
    @GetMapping("/demarcacion/{demarcacionId}/anio/{anio}")
    public ResponseEntity<?> getDataFechaDemarcacion(
            @PathVariable Integer demarcacionId,
            @PathVariable Integer anio) {
        try {
            List<IndicadorDemarcacionFechaDataProjection> datos = reporteUtSequiaService.getAllDataFechaDemarcacion(demarcacionId, anio);
            return ResponseEntity.ok(datos);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

}