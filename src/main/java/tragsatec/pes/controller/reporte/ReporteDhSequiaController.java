package tragsatec.pes.controller.reporte;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tragsatec.pes.dto.calculo.IndicadorDataProjection;
import tragsatec.pes.dto.calculo.IndicadorFechaDataProjection;
import tragsatec.pes.service.reporte.ReporteDhSequiaService;

import java.util.List;

@RestController
@RequestMapping("/reportes/dh-sequia")
public class ReporteDhSequiaController {

    private final ReporteDhSequiaService reporteDhSequiaService;

    @Autowired
    public ReporteDhSequiaController(ReporteDhSequiaService reporteDhSequiaService) {
        this.reporteDhSequiaService = reporteDhSequiaService;
    }

    @GetMapping("/{demarcacionId}")
    public ResponseEntity<?> getDataIndicadorAnioMes(
            @PathVariable Integer demarcacionId,
            @RequestParam String tipoPrep) {
        try {
            List<IndicadorDataProjection> datos = reporteDhSequiaService.getAllDataIndicadorAnioMes(demarcacionId, tipoPrep);
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
            List<IndicadorFechaDataProjection> datos = reporteDhSequiaService.getAllDataFecha(anio, tipoPrep);
            return ResponseEntity.ok(datos);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}