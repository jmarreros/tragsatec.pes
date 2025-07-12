package tragsatec.pes.controller.general;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tragsatec.pes.dto.general.DemarcacionProjection;
import tragsatec.pes.dto.general.DemarcacionResponseDTO;
import tragsatec.pes.dto.general.DemarcacionSummaryDTO; // Asegúrate de que está importado
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
    public List<DemarcacionSummaryDTO> getAll() {
        return demarcacionService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<DemarcacionResponseDTO> getById(@PathVariable Integer id) {
        return demarcacionService.findByIdWithUnidadesTerritoriales(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public DemarcacionSummaryDTO create(@RequestBody DemarcacionEntity entity) {
        return demarcacionService.save(entity);
    }

    @PutMapping("/{id}")
    public ResponseEntity<DemarcacionSummaryDTO> update(@PathVariable Integer id, @RequestBody DemarcacionEntity entity) { // Tipo de retorno cambiado
        if (demarcacionService.findById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        entity.setId(id);
        DemarcacionSummaryDTO updatedDto = demarcacionService.save(entity);
        return ResponseEntity.ok(updatedDto);
    }


    @GetMapping("/por-tipo")
    public ResponseEntity<List<DemarcacionProjection>> getDemarcacionesByTipo(@RequestParam Character tipo) {
        List<DemarcacionProjection> demarcaciones = demarcacionService.findDemarcacionesByTipo(tipo);
        return ResponseEntity.ok(demarcaciones);
    }
}