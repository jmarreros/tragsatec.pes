package tragsatec.pes.controller.estructura;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tragsatec.pes.persistence.entity.estructura.PesDemarcacionUtEntity;
import tragsatec.pes.service.estructura.PesDemarcacionUtService; // Asume que este servicio existe

import java.util.List;

@RestController
@RequestMapping("pes-demarcacion-ut")
@RequiredArgsConstructor
public class PesDemarcacionUtController {

    private final PesDemarcacionUtService pesDemarcacionUtService;

    @GetMapping
    public ResponseEntity<List<PesDemarcacionUtEntity>> getAllPesDemarcacionUts() {
        List<PesDemarcacionUtEntity> entities = pesDemarcacionUtService.findAll();
        return ResponseEntity.ok(entities);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PesDemarcacionUtEntity> getPesDemarcacionUtById(@PathVariable Integer id) {
        return pesDemarcacionUtService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<PesDemarcacionUtEntity> createPesDemarcacionUt(@RequestBody PesDemarcacionUtEntity pesDemarcacionUtEntity) {
        PesDemarcacionUtEntity savedEntity = pesDemarcacionUtService.save(pesDemarcacionUtEntity);
        return new ResponseEntity<>(savedEntity, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PesDemarcacionUtEntity> updatePesDemarcacionUt(@PathVariable Integer id, @RequestBody PesDemarcacionUtEntity pesDemarcacionUtEntity) {
        return pesDemarcacionUtService.update(id, pesDemarcacionUtEntity)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

}
