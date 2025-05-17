package tragsatec.pes.controller.general;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tragsatec.pes.persistence.entity.general.EstacionEntity;
import tragsatec.pes.service.general.EstacionService;

import java.util.List;

@RestController
@RequestMapping("estaciones")
public class EstacionController {
    private final EstacionService estacionService;

    @Autowired
    public EstacionController(EstacionService estacionService) {
        this.estacionService = estacionService;
    }

    @GetMapping
    public List<EstacionEntity> getAll() {
        return estacionService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<EstacionEntity> getById(@PathVariable String id) {
        return estacionService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public EstacionEntity create(@RequestBody EstacionEntity entity) {
        return estacionService.save(entity);
    }

    @PutMapping("/{id}")
    public ResponseEntity<EstacionEntity> update(@PathVariable String id, @RequestBody EstacionEntity entity) {
        if (estacionService.findById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        entity.setId(id);
        return ResponseEntity.ok(estacionService.save(entity));
    }
}

