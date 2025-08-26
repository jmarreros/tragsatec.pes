package com.chc.pes.controller.estructura;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.chc.pes.dto.estructura.PesUmbralEscasezDTO;
import com.chc.pes.service.estructura.PesUmbralEscasezService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/pes-umbral-escasez")
@RequiredArgsConstructor
public class PesUmbralEscasezController {

    private final PesUmbralEscasezService pesUmbralEscasezService;

    @GetMapping
    public ResponseEntity<List<PesUmbralEscasezDTO>> getAll() {
        List<PesUmbralEscasezDTO> dtos = pesUmbralEscasezService.findAll();
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PesUmbralEscasezDTO> getById(@PathVariable Integer id) {
        return pesUmbralEscasezService.findByIdDto(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<PesUmbralEscasezDTO> create(@RequestBody PesUmbralEscasezDTO dto) {
        PesUmbralEscasezDTO savedDto = pesUmbralEscasezService.save(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedDto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PesUmbralEscasezDTO> update(@PathVariable Integer id, @RequestBody PesUmbralEscasezDTO dto) {
        return pesUmbralEscasezService.update(id, dto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}

