package tragsatec.pes.controller.reporte;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tragsatec.pes.dto.calculo.IndicadorDataProjection;
import tragsatec.pes.dto.calculo.IndicadorDemarcacionFechaDataProjection;
import tragsatec.pes.dto.calculo.IndicadorFechaDataProjection;
import tragsatec.pes.dto.calculo.IndicadorUTFechaDataProjection;
import tragsatec.pes.service.reporte.ReporteUtEscasezService;

import java.util.List;

@RestController
@RequestMapping("/reportes/ut-escasez")
public class ReporteUtEscasezController {

    private final ReporteUtEscasezService reporteUtEscasezService;

    @Autowired
    public ReporteUtEscasezController(ReporteUtEscasezService reporteUtEscasezService) {
        this.reporteUtEscasezService = reporteUtEscasezService;
    }

    @GetMapping("/{utId}")
    public ResponseEntity<?> getDataIndicadorAnioMes(@PathVariable Integer utId) {
        try {
            List<IndicadorDataProjection> datos = reporteUtEscasezService.getAllDataIndicadorAnioMes(utId);
            return ResponseEntity.ok(datos);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/fecha/{anio}")
    public ResponseEntity<?> getDataFecha(@PathVariable Integer anio) {
        try {
            List<IndicadorFechaDataProjection> datos = reporteUtEscasezService.getAllDataFecha(anio);
            return ResponseEntity.ok(datos);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/demarcacion/{demarcacionId}/anio/{anio}")
    public ResponseEntity<?> getDataFechaDemarcacion(
            @PathVariable Integer demarcacionId,
            @PathVariable Integer anio) {
        try {
            List<IndicadorDemarcacionFechaDataProjection> datos = reporteUtEscasezService.getAllDataFechaDemarcacion(demarcacionId, anio);
            return ResponseEntity.ok(datos);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/total/{utId}/anio/{anio}")
    public ResponseEntity<?> getTotalDataUTFecha(
            @PathVariable Integer utId,
            @PathVariable Integer anio) {
        try {
            List<IndicadorUTFechaDataProjection> datos = reporteUtEscasezService.getTotalDataUTFecha(utId, anio);
            return ResponseEntity.ok(datos);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}