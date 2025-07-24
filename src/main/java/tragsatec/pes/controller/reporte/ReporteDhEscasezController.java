package tragsatec.pes.controller.reporte;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tragsatec.pes.dto.calculo.IndicadorDataProjection;
import tragsatec.pes.service.reporte.ReporteDhEscasezService;

import java.util.List;

@RestController
@RequestMapping("/reportes/dh-escasez")
public class ReporteDhEscasezController {

    private final ReporteDhEscasezService reporteDhEscasezService;

    @Autowired
    public ReporteDhEscasezController(ReporteDhEscasezService reporteDhEscasezService) {
        this.reporteDhEscasezService = reporteDhEscasezService;
    }

    @GetMapping("/{demarcacionId}")
    public ResponseEntity<?> getDataIndicadorAnioMes(@PathVariable Integer demarcacionId) {
        try {
            List<IndicadorDataProjection> datos = reporteDhEscasezService.getAllDataIndicadorAnioMes(demarcacionId);
            return ResponseEntity.ok(datos);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}