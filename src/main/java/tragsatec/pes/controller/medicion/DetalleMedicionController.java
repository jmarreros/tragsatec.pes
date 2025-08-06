package tragsatec.pes.controller.medicion;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tragsatec.pes.dto.medicion.DetalleMedicionProjection;
import tragsatec.pes.service.medicion.DetalleMedicionService;

import java.util.List;

@RestController
@RequestMapping("/detalles-medicion")
@RequiredArgsConstructor
public class DetalleMedicionController {

    private final DetalleMedicionService detalleMedicionService;

    @GetMapping("/{medicionId}/reporte-detalles")
    public ResponseEntity<List<DetalleMedicionProjection>> getReporteDetallesPorMedicionId(@PathVariable Integer medicionId) {
        List<DetalleMedicionProjection> reporte = detalleMedicionService.findReporteByMedicionId(medicionId);
        return ResponseEntity.ok(reporte);
    }
}