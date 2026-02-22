package com.chc.pes.controller.reporte;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.chc.pes.dto.calculo.IndicadorDataProjection;
import com.chc.pes.dto.calculo.IndicadorFechaDataProjection;
import com.chc.pes.service.reporte.ReporteDhSequiaService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/reportes/dh-sequia")
@Slf4j
public class ReporteDhSequiaController {

    private final ReporteDhSequiaService reporteDhSequiaService;

    @Autowired
    public ReporteDhSequiaController(ReporteDhSequiaService reporteDhSequiaService) {
        this.reporteDhSequiaService = reporteDhSequiaService;
    }

    @GetMapping("/{demarcacionId}")
    public ResponseEntity<?> getDataIndicadorAnioMes(
            @PathVariable Integer demarcacionId,
            @RequestParam String tipoPrep) {
        try {
            List<IndicadorDataProjection> datos = reporteDhSequiaService.getAllDataIndicadorAnioMes(demarcacionId, tipoPrep);
            return ResponseEntity.ok(datos);
        } catch (IllegalArgumentException e) {
            log.error(e.getMessage());
            return ResponseEntity.badRequest().body("Error al obtener los indicadores");
        }
    }

    @GetMapping("/fecha/{anio}")
    public ResponseEntity<?> getDataFecha(
            @PathVariable Integer anio,
            @RequestParam String tipoPrep) {
        try {
            List<IndicadorFechaDataProjection> datos = reporteDhSequiaService.getAllDataFecha(anio, tipoPrep);
            return ResponseEntity.ok(datos);
        } catch (IllegalArgumentException e) {
            log.error(e.getMessage());
            return ResponseEntity.badRequest().body("Error al obtener los indicadores por fecha");
        }
    }
}