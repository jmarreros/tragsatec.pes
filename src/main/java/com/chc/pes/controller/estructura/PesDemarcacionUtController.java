package com.chc.pes.controller.estructura;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.chc.pes.dto.estructura.PesDemarcacionUtRequestDTO;
import com.chc.pes.dto.estructura.PesDemarcacionUtResponseDTO; // Importar el nuevo DTO de respuesta
import com.chc.pes.service.estructura.PesDemarcacionUtService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/pes-demarcacion-ut")
@RequiredArgsConstructor
public class PesDemarcacionUtController {

    private final PesDemarcacionUtService pesDemarcacionUtService;

    @GetMapping
    public ResponseEntity<List<PesDemarcacionUtResponseDTO>> getAllPesDemarcacionUts() {
        List<PesDemarcacionUtResponseDTO> dtos = pesDemarcacionUtService.findAll();
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PesDemarcacionUtResponseDTO> getPesDemarcacionUtById(@PathVariable Integer id) {
        return pesDemarcacionUtService.findByIdDto(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<PesDemarcacionUtResponseDTO> createPesDemarcacionUt(@RequestBody PesDemarcacionUtRequestDTO dto) {
        PesDemarcacionUtResponseDTO savedDto = pesDemarcacionUtService.save(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedDto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PesDemarcacionUtResponseDTO> updatePesDemarcacionUt(@PathVariable Integer id, @RequestBody PesDemarcacionUtRequestDTO dto) { // Cuerpo de la solicitud cambiado a DTO
        return pesDemarcacionUtService.update(id, dto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
