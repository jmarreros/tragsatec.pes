package tragsatec.pes.controller.estructura;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tragsatec.pes.persistence.entity.estructura.PesUtEstacionEntity;
import tragsatec.pes.service.estructura.PesUtEstacionService; // Asume que este servicio existe

import java.util.List;

@RestController
@RequestMapping("pes-ut-estacion")
@RequiredArgsConstructor
public class PesUtEstacionController {

    private final PesUtEstacionService pesUtEstacionService;

    @GetMapping
    public ResponseEntity<List<PesUtEstacionEntity>> getAllPesUtEstaciones() {
        List<PesUtEstacionEntity> entities = pesUtEstacionService.findAll();
        return ResponseEntity.ok(entities);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PesUtEstacionEntity> getPesUtEstacionById(@PathVariable Integer id) {
        return pesUtEstacionService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<PesUtEstacionEntity> createPesUtEstacion(@RequestBody PesUtEstacionEntity pesUtEstacionEntity) {
        PesUtEstacionEntity savedEntity = pesUtEstacionService.save(pesUtEstacionEntity);
        return new ResponseEntity<>(savedEntity, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PesUtEstacionEntity> updatePesUtEstacion(@PathVariable Integer id, @RequestBody PesUtEstacionEntity pesUtEstacionEntity) {
        return pesUtEstacionService.update(id, pesUtEstacionEntity)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
