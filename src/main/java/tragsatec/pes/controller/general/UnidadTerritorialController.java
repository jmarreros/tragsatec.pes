package tragsatec.pes.controller.general;

import jakarta.persistence.EntityNotFoundException; // Importar si se maneja aquí
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tragsatec.pes.dto.general.UnidadTerritorialRequestDTO;
import tragsatec.pes.dto.general.UnidadTerritorialResponseDTO; // Cambiado
import tragsatec.pes.dto.general.UnidadTerritorialSummaryDTO;
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
    public List<UnidadTerritorialSummaryDTO> getAll() {
        return unidadTerritorialService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<UnidadTerritorialResponseDTO> getById(@PathVariable Integer id) { // Tipo de respuesta cambiado
        return unidadTerritorialService.findByIdAsDto(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody UnidadTerritorialRequestDTO request) {
        try {
            UnidadTerritorialResponseDTO createdDto = unidadTerritorialService.createUnidadTerritorial(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdDto);
        } catch (EntityNotFoundException e) { // Si alguna entidad referenciada no se encuentra
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) { // Manejo genérico de otras posibles excepciones
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al crear la unidad territorial: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Integer id, @RequestBody UnidadTerritorialRequestDTO requestDTO) {
        try {
            UnidadTerritorialResponseDTO updatedDto = unidadTerritorialService.updateUnidadTerritorial(id, requestDTO);
            return ResponseEntity.ok(updatedDto);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) { // Manejo genérico
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al actualizar la unidad territorial: " + e.getMessage());
        }
    }
}