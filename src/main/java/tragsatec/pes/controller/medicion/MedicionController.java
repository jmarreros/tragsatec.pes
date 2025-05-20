package tragsatec.pes.controller.medicion;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tragsatec.pes.dto.medicion.MedicionDTO;
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

}

