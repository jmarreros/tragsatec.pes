package tragsatec.pes.controller.medicion;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tragsatec.pes.dto.medicion.MedicionDTO;
import tragsatec.pes.dto.medicion.MedicionHistorialProjection;
import tragsatec.pes.dto.medicion.SiguienteMedicionDTO;
import tragsatec.pes.service.medicion.MedicionService;

import java.util.List;

@RestController
@RequestMapping("medicion")
@RequiredArgsConstructor
public class MedicionController {

    private final MedicionService medicionService;

    @GetMapping
    public ResponseEntity<List<MedicionDTO>> getAll() {
        List<MedicionDTO> dtos = medicionService.findAll();
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MedicionDTO> getById(@PathVariable Integer id) {
        return medicionService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<MedicionDTO> create(@RequestBody MedicionDTO dto) {
        MedicionDTO savedDto = medicionService.save(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedDto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<MedicionDTO> update(@PathVariable Integer id, @RequestBody MedicionDTO dto) {
        return medicionService.update(id, dto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }


    // Busca la primera medición no procesada, si no la encuentra construye una nueva medición
    @GetMapping("/pendiente-nueva")
    public ResponseEntity<?> getFirstNotProcessedMedicion(@RequestParam("tipo") Character tipo) {
        try {
            MedicionDTO medicion = medicionService.findFirstNotProcessedMedicionByTipo(tipo);
            if (medicion != null) {
                return ResponseEntity.ok(medicion);
            } else {
                SiguienteMedicionDTO siguienteMedicion = medicionService.findSiguienteMedicion(tipo);
                if (siguienteMedicion != null) {
                    // Construir el objeto de medicion
                    MedicionDTO medicionNueva = new MedicionDTO();
                    medicionNueva.setTipo(tipo);
                    medicionNueva.setAnio(siguienteMedicion.getAnio());
                    medicionNueva.setMes(siguienteMedicion.getMes());

                    return ResponseEntity.ok(medicionNueva);
                } else {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body("No se encontró una medición pendiente ni una nueva medición para el tipo: " + tipo);
                }
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al buscar la primera medición no procesada: " + e.getMessage());
        }
    }

    @GetMapping("/historial")
    public ResponseEntity<List<MedicionHistorialProjection>> getHistorial(
            @RequestParam("anio") Short anio,
            @RequestParam("tipo") Character tipo) {
        List<MedicionHistorialProjection> historial = medicionService.getHistorialMediciones(anio, tipo);
        return ResponseEntity.ok(historial);
    }

    @GetMapping("/ultima-procesada")
    public ResponseEntity<MedicionDTO> getLastProcessedMedicion(@RequestParam("tipo") Character tipo) {
        return medicionService.findLastProcessedMedicionByTipo(tipo)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}

