package tragsatec.pes.controller.general;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tragsatec.pes.dto.general.EstacionRequestDTO;
import tragsatec.pes.dto.general.EstacionResponseDTO;
import tragsatec.pes.service.general.EstacionService;

import java.util.List;
// Si getAll() también va a devolver DTOs, necesitarás un mapeo para la lista.
// import java.util.stream.Collectors;

@RestController
@RequestMapping("estaciones")
public class EstacionController {
    private final EstacionService estacionService;

    @Autowired
    public EstacionController(EstacionService estacionService) {
        this.estacionService = estacionService;
    }

    // Si getAll devuelve DTOs:
     @GetMapping
     public List<EstacionResponseDTO> getAll() {
         return estacionService.findAll();
     }

    @GetMapping("/{id}")
    public ResponseEntity<EstacionResponseDTO> getById(@PathVariable Integer id) { // Cambiado a EstacionResponseDTO
        return estacionService.findByIdAsDto(id) // Usar el método que devuelve DTO
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody EstacionRequestDTO dto) {
        try {
            EstacionResponseDTO createdEstacion = estacionService.createEstacionAndUnidadesTerritoriales(dto); // Recibe DTO
            return ResponseEntity.status(HttpStatus.CREATED).body(createdEstacion);
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (IllegalArgumentException | EntityNotFoundException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(
            @PathVariable Integer id,
            @RequestBody EstacionRequestDTO dto) {
        try {
            EstacionResponseDTO updatedEstacion = estacionService.updateEstacionAndUnidadesTerritoriales(id, dto); // Recibe DTO
            return ResponseEntity.ok(updatedEstacion);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}