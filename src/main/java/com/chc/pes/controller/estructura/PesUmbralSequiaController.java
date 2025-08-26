package com.chc.pes.controller.estructura;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.chc.pes.dto.estructura.PesUmbralSequiaDTO;
import com.chc.pes.service.estructura.PesUmbralSequiaService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/pes-umbral-sequia")
@RequiredArgsConstructor
public class PesUmbralSequiaController {

    private final PesUmbralSequiaService pesUmbralSequiaService;

    @GetMapping
    public ResponseEntity<List<PesUmbralSequiaDTO>> getAll() {
        List<PesUmbralSequiaDTO> dtos = pesUmbralSequiaService.findAll();
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PesUmbralSequiaDTO> getById(@PathVariable Integer id) {
        return pesUmbralSequiaService.findByIdDto(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<PesUmbralSequiaDTO> create(@RequestBody PesUmbralSequiaDTO dto) {
        PesUmbralSequiaDTO savedDto = pesUmbralSequiaService.save(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedDto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PesUmbralSequiaDTO> update(@PathVariable Integer id, @RequestBody PesUmbralSequiaDTO dto) {
        return pesUmbralSequiaService.update(id, dto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

}

