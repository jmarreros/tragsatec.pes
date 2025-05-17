package tragsatec.pes.controller.general;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tragsatec.pes.persistence.entity.general.DemarcacionEntity;
import tragsatec.pes.service.general.DemarcacionService;

import java.util.List;

@RestController
@RequestMapping("demarcaciones")
public class DemarcacionController {
    private final DemarcacionService demarcacionService;

    @Autowired
    public DemarcacionController(DemarcacionService demarcacionService) {
        this.demarcacionService = demarcacionService;
    }

    @GetMapping
    public List<DemarcacionEntity> getAll() {
        return demarcacionService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<DemarcacionEntity> getById(@PathVariable Integer id) {
        return demarcacionService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public DemarcacionEntity create(@RequestBody DemarcacionEntity entity) {
        return demarcacionService.save(entity);
    }

    @PutMapping("/{id}")
    public ResponseEntity<DemarcacionEntity> update(@PathVariable Integer id, @RequestBody DemarcacionEntity entity) {
        if (demarcacionService.findById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        entity.setId(id);
        return ResponseEntity.ok(demarcacionService.save(entity));
    }
}

