package tragsatec.pes.controller.estructura;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tragsatec.pes.persistence.entity.estructura.PesEntity;
import tragsatec.pes.service.estructura.PesService;

import java.util.Optional;

@RestController
@RequestMapping("pes")
public class PesController {
    @Autowired
    private PesService pesService;

    @GetMapping
    public ResponseEntity<Iterable<PesEntity>> getAll() {
        return ResponseEntity.ok(pesService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PesEntity> getById(@PathVariable Integer id) {
        return pesService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<PesEntity> create(@RequestBody PesEntity pesEntity) {
        return ResponseEntity.ok(pesService.save(pesEntity));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Optional<PesEntity>> update(@PathVariable Integer id, @RequestBody PesEntity pesEntity) {
        return ResponseEntity.ok(pesService.update(id, pesEntity));
    }
}

