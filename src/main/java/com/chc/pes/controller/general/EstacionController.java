package com.chc.pes.controller.general;

import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.chc.pes.dto.general.EstacionProjection;
import com.chc.pes.dto.general.EstacionRequestDTO;
import com.chc.pes.dto.general.EstacionResponseDTO;
import com.chc.pes.service.general.EstacionService;
import com.chc.pes.service.medicion.MedicionService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/estaciones")
@Slf4j
public class EstacionController {
    private final EstacionService estacionService;
    private final MedicionService medicionService;

    @Autowired
    public EstacionController(EstacionService estacionService, MedicionService medicionService) {
        this.estacionService = estacionService;
        this.medicionService = medicionService;
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
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Error de integridad de datos");
        } catch (IllegalArgumentException | EntityNotFoundException e) {
            log.error(e.getMessage());
            return ResponseEntity.badRequest().body("Error al crear la estación ");
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
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Error al actualizar");
        } catch (DataIntegrityViolationException e) {
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Error de integridad de datos");
        } catch (IllegalArgumentException e) {
            log.error(e.getMessage());
            return ResponseEntity.badRequest().body("Error al actualizar la estacion");
        }
    }

    // Obtener las estaciones por plan de sequia actual y por tipo
    @GetMapping("/pes")
    public ResponseEntity<List<EstacionProjection>> getEstacionesPorPes(
            @RequestParam("tipo") Character tipo) {
        List<EstacionProjection> estaciones = estacionService.getEstacionesByTipoCurrentPes(tipo);
        return ResponseEntity.ok(estaciones);
    }

    // Obtener todas las estaciones por tipo
    @GetMapping("/por-tipo")
    public ResponseEntity<List<EstacionProjection>> getEstacionesPorTipo(
            @RequestParam("tipo") Character tipo) {
        List<EstacionProjection> estaciones = estacionService.getEstacionesByTipo(tipo);
        return ResponseEntity.ok(estaciones);
    }
}