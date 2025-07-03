package tragsatec.pes.controller.medicion;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tragsatec.pes.dto.medicion.DetalleMedicionDTO;
import tragsatec.pes.dto.medicion.DetalleMedicionProjection;
import tragsatec.pes.service.medicion.DetalleMedicionService;

import java.util.List;

@RestController
@RequestMapping("detalles-medicion")
@RequiredArgsConstructor
public class DetalleMedicionController {

    private final DetalleMedicionService detalleMedicionService;

    @GetMapping
    public ResponseEntity<List<DetalleMedicionDTO>> getAll() {
        List<DetalleMedicionDTO> dtos = detalleMedicionService.findAll();
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DetalleMedicionDTO> getById(@PathVariable Long id) {
        return detalleMedicionService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<DetalleMedicionDTO> create(@RequestBody DetalleMedicionDTO dto) {
        DetalleMedicionDTO savedDto = detalleMedicionService.save(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedDto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<DetalleMedicionDTO> update(@PathVariable Long id, @RequestBody DetalleMedicionDTO dto) {
        return detalleMedicionService.update(id, dto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }


    @GetMapping("/{medicionId}/reporte-detalles")
    public ResponseEntity<List<DetalleMedicionProjection>> getReporteDetalles(
            @PathVariable("medicionId") Integer medicionId) {
        List<DetalleMedicionProjection> reporte = detalleMedicionService.getReporteDetallesPorMedicion(medicionId);
        return ResponseEntity.ok(reporte);
    }
}