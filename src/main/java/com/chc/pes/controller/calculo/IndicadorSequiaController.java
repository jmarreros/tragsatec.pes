package com.chc.pes.controller.calculo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.chc.pes.service.calculo.IndicadorSequiaService;

@Slf4j
@RestController
@RequestMapping("/api/v1/indicadores/sequia")
@RequiredArgsConstructor
public class IndicadorSequiaController {

    private final IndicadorSequiaService indicadorSequiaService;

    @PostMapping("/calcular")
    public ResponseEntity<String> calcularIndicador() {
        try {
            indicadorSequiaService.calcularIndicadorSequia();
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.internalServerError().body("Error al calcular el indicador de sequía");
        }
    }

    @DeleteMapping("/medicion/no-procesado")
    public ResponseEntity<Void> limpiarIndicadoresMedicionNoProcesada() {
        try {
            indicadorSequiaService.limpiarIndicadoresMedicionNoProcesada();
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

}
