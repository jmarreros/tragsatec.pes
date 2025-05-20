package tragsatec.pes.controller.medicion;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tragsatec.pes.dto.medicion.ArchivoMedicionDTO;
import tragsatec.pes.service.medicion.ArchivoMedicionService;

import java.util.List;

@RestController
@RequestMapping("archivo-medicion") // Endpoint base para esta entidad
@RequiredArgsConstructor
public class ArchivoMedicionController {

    private final ArchivoMedicionService archivoMedicionService;

    @GetMapping
    public ResponseEntity<List<ArchivoMedicionDTO>> getAll() {
        return ResponseEntity.ok(archivoMedicionService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ArchivoMedicionDTO> getById(@PathVariable Integer id) {
        return archivoMedicionService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<ArchivoMedicionDTO> create(@RequestBody ArchivoMedicionDTO dto) {
        ArchivoMedicionDTO savedDto = archivoMedicionService.save(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedDto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ArchivoMedicionDTO> update(@PathVariable Integer id, @RequestBody ArchivoMedicionDTO dto) {
        return archivoMedicionService.update(id, dto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

}

