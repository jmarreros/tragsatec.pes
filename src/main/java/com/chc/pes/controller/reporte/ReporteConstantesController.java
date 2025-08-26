package com.chc.pes.controller.reporte;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.chc.pes.service.reporte.ReporteConstantesService;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/reportes/constantes")
public class ReporteConstantesController {

    private final ReporteConstantesService reporteConstantesService;

    @Autowired
    public ReporteConstantesController(ReporteConstantesService reporteConstantesService) {
        this.reporteConstantesService = reporteConstantesService;
    }

    @GetMapping("/sequia")
    public ResponseEntity<Map<String, BigDecimal>> getConstantesDeSequia() {
        Map<String, BigDecimal> constantes = reporteConstantesService.getConstantesDeSequia();
        return ResponseEntity.ok(constantes);
    }

    @GetMapping("/escasez")
    public ResponseEntity<Map<String, Object>> getConstantesDeEscasez() {
        Map<String, Object> constantes = reporteConstantesService.getConstantesDeEscasez();
        return ResponseEntity.ok(constantes);
    }
}