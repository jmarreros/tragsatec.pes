package tragsatec.pes.controller.general;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tragsatec.pes.persistence.entity.general.UnidadTerritorialEntity;
import tragsatec.pes.service.general.UnidadTerritorialService;

import java.util.List;

@RestController
@RequestMapping("unidades-territoriales")
public class UnidadTerritorialController {
    private final UnidadTerritorialService unidadTerritorialService;

    @Autowired
    public UnidadTerritorialController(UnidadTerritorialService unidadTerritorialService) {
        this.unidadTerritorialService = unidadTerritorialService;
    }

    @GetMapping
    public List<UnidadTerritorialEntity> getAll() {
        return unidadTerritorialService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<UnidadTerritorialEntity> getById(@PathVariable String id) {
        return unidadTerritorialService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public UnidadTerritorialEntity create(@RequestBody UnidadTerritorialEntity entity) {
        return unidadTerritorialService.save(entity);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UnidadTerritorialEntity> update(@PathVariable String id, @RequestBody UnidadTerritorialEntity entity) {
        if (unidadTerritorialService.findById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        entity.setId(id);
        return ResponseEntity.ok(unidadTerritorialService.save(entity));
    }
}

