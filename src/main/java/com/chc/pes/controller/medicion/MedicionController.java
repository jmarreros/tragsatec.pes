package com.chc.pes.controller.medicion;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.chc.pes.dto.medicion.MedicionDTO;
import com.chc.pes.dto.medicion.MedicionHistorialProjection;
import com.chc.pes.service.medicion.MedicionService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/medicion")
@RequiredArgsConstructor
@Slf4j
public class MedicionController {

    private final MedicionService medicionService;

    // Busca la primera medición no procesada, si no la hay, busca la siguiente medición nueva
    @GetMapping("/pendiente-nueva")
    public ResponseEntity<?> getNotProcessedMedicion(@RequestParam("tipo") Character tipo) {
        try {
            MedicionDTO medicion = medicionService.obtenerMedicionPendienteONueva(tipo);
            if (medicion != null) {
                return ResponseEntity.ok(medicion);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("No se encontró una medición pendiente ni una nueva medición para el tipo: " + tipo);
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al buscar la primera medición no procesada");
        }
    }

    @GetMapping("/historial")
    public ResponseEntity<List<MedicionHistorialProjection>> getHistorial(
            @RequestParam("anio") Short anio,
            @RequestParam("tipo") Character tipo) {
        try {
            List<MedicionHistorialProjection> historial = medicionService.getHistorialMediciones(anio, tipo);
            return ResponseEntity.ok(historial);
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/ultima-procesada")
    public ResponseEntity<MedicionDTO> getLastProcessedMedicion(@RequestParam("tipo") Character tipo) {
        try {
            return medicionService.findLastProcessedMedicionByTipo(tipo).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/ultimas5")
    public List<MedicionHistorialProjection> getUltimas5MedicionesPorTipo(@RequestParam("tipo") Character tipo) {
        try {
            return medicionService.getUltimas5MedicionesPorTipo(tipo);
        } catch (Exception e) {
            log.error(e.getMessage());
            return null;
        }
    }

}

