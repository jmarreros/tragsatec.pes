package com.chc.pes.controller.estructura;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.chc.pes.dto.estructura.PesUtEstacionRequestDTO;
import com.chc.pes.dto.estructura.PesUtEstacionResponseDTO;
import com.chc.pes.service.estructura.PesUtEstacionService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/pes-ut-estacion") // Ajusta la ruta base según sea necesario
@RequiredArgsConstructor
public class PesUtEstacionController {

    private final PesUtEstacionService pesUtEstacionService;

    @GetMapping
    public ResponseEntity<List<PesUtEstacionResponseDTO>> getAllPesUtEstaciones() {
        List<PesUtEstacionResponseDTO> dtos = pesUtEstacionService.findAll();
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PesUtEstacionResponseDTO> getPesUtEstacionById(@PathVariable Integer id) {
        return pesUtEstacionService.findByIdDto(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<PesUtEstacionResponseDTO> createPesUtEstacion(@RequestBody PesUtEstacionRequestDTO dto) {
        PesUtEstacionResponseDTO savedDto = pesUtEstacionService.save(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedDto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PesUtEstacionResponseDTO> updatePesUtEstacion(@PathVariable Integer id, @RequestBody PesUtEstacionRequestDTO dto) {
        return pesUtEstacionService.update(id, dto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
