package tragsatec.pes.controller.reporte;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tragsatec.pes.dto.calculo.IndicadorDataProjection;
import tragsatec.pes.dto.reporte.EstadisticasMensualesDTO;
import tragsatec.pes.dto.reporte.EstadisticasMensualesEscasezDTO;
import tragsatec.pes.service.reporte.ReporteEstacionEscasezService;

import java.util.List;

@RestController
@RequestMapping("/reportes/estacion-escasez")
public class ReporteEstacionEscasezController {

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
}