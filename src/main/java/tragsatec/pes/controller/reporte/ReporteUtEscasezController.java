package tragsatec.pes.controller.reporte;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tragsatec.pes.dto.calculo.IndicadorDataProjection;
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
}