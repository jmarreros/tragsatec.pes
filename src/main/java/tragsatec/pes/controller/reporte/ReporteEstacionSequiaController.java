package tragsatec.pes.controller.reporte;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tragsatec.pes.dto.calculo.IndicadorDataProjection;
import tragsatec.pes.service.reporte.ReporteEstacionSequiaService;

import java.util.List;

@RestController
@RequestMapping("/reportes/estacion-sequia")
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
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}