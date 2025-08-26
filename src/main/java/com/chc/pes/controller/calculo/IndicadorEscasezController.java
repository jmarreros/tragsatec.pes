package com.chc.pes.controller.calculo;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.chc.pes.service.calculo.IndicadorEscasezService;

@RestController
@RequestMapping("/api/v1/indicadores/escasez")
@RequiredArgsConstructor
public class IndicadorEscasezController {

    private final IndicadorEscasezService indicadorEscasezService;

    @PostMapping("/calcular")
    public ResponseEntity<String> calcularIndicador() {
        try {
            indicadorEscasezService.calcularIndicadorEscasez();
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }


    @DeleteMapping("/medicion/no-procesado")
    public ResponseEntity<Void> limpiarIndicadoresMedicionNoProcesada() {
        try {
            indicadorEscasezService.limpiarIndicadoresMedicionNoProcesada();
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

}