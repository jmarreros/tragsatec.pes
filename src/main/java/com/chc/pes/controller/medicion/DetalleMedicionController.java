package com.chc.pes.controller.medicion;

import com.chc.pes.dto.medicion.DetalleMedicionDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.chc.pes.dto.medicion.DetalleMedicionProjection;
import com.chc.pes.service.medicion.DetalleMedicionService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/detalles-medicion")
@RequiredArgsConstructor
public class DetalleMedicionController {

    private final DetalleMedicionService detalleMedicionService;

    @GetMapping("/{medicionId}/reporte-detalles")
    public ResponseEntity<List<DetalleMedicionProjection>> getReporteDetallesPorMedicionId(@PathVariable Integer medicionId) {
        List<DetalleMedicionProjection> reporte = detalleMedicionService.findReporteByMedicionId(medicionId);
        return ResponseEntity.ok(reporte);
    }

    // Grabar o actualizar detalle de medicion por medicionId y estacionId
    @PutMapping("/{medicionId}/actualizar-detalles/{estacionId}")
    public ResponseEntity<DetalleMedicionDTO> actualizarDetalleMedicion(
            @PathVariable Integer medicionId,
            @PathVariable Integer estacionId,
            @RequestBody DetalleMedicionDTO detalleMedicionDTO) {

        DetalleMedicionDTO detalleActualizado = detalleMedicionService.actualizarDetalleMedicion(
                medicionId, estacionId, detalleMedicionDTO);

        return ResponseEntity.ok(detalleActualizado);
    }
}