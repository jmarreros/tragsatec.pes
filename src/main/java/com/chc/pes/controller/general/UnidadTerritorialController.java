package com.chc.pes.controller.general;

import jakarta.persistence.EntityNotFoundException; // Importar si se maneja aquí
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.chc.pes.dto.general.UnidadTerritorialProjection;
import com.chc.pes.dto.general.UnidadTerritorialRequestDTO;
import com.chc.pes.dto.general.UnidadTerritorialResponseDTO; // Cambiado
import com.chc.pes.dto.general.UnidadTerritorialSummaryDTO;
import com.chc.pes.service.general.UnidadTerritorialService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/unidades-territoriales")
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

    @GetMapping("/por-tipo")
    public ResponseEntity<List<UnidadTerritorialProjection>> getUnidadesTerritorialesByTipo(@RequestParam Character tipo) {
        List<UnidadTerritorialProjection> unidades = unidadTerritorialService.getUnidadesTerritorialesByTipo(tipo);
        return ResponseEntity.ok(unidades);
    }

    @GetMapping("/por-tipo-demarcacion")
    public ResponseEntity<List<UnidadTerritorialProjection>> getUnidadesTerritorialesByTipoAndDemarcacion(
            @RequestParam Character tipo,
            @RequestParam Integer demarcacion) {
        List<UnidadTerritorialProjection> unidades = unidadTerritorialService.getUnidadesTerritorialesByTipoAndDemarcacion(tipo, demarcacion);
        return ResponseEntity.ok(unidades);
    }
}