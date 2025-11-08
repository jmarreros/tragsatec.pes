package com.chc.pes.controller.reporte;

import com.chc.pes.service.reporte.ReporteWordUtSequiaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.chc.pes.dto.calculo.IndicadorDataProjection;
import com.chc.pes.dto.calculo.IndicadorDemarcacionFechaDataProjection;
import com.chc.pes.dto.calculo.IndicadorFechaDataProjection;
import com.chc.pes.dto.calculo.IndicadorUTFechaDataProjection;
import com.chc.pes.service.reporte.ReporteUtSequiaService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/reportes/ut-sequia")
public class ReporteUtSequiaController {

    private final ReporteUtSequiaService reporteUtSequiaService;
    private final ReporteWordUtSequiaService reporteWordUtSequiaService;

    @Autowired
    public ReporteUtSequiaController(ReporteUtSequiaService reporteUtSequiaService, ReporteWordUtSequiaService reporteWordUtSequiaService) {
        this.reporteUtSequiaService = reporteUtSequiaService;
        this.reporteWordUtSequiaService = reporteWordUtSequiaService;
    }

    @GetMapping("/{utId}")
    public ResponseEntity<?> getDataIndicadorAnioMes(
            @PathVariable Integer utId,
            @RequestParam String tipoPrep) {
        try {
            List<IndicadorDataProjection> datos = reporteUtSequiaService.getAllDataIndicadorAnioMes(utId, tipoPrep);
            return ResponseEntity.ok(datos);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/fecha/{anio}")
    public ResponseEntity<?> getDataFecha(
            @PathVariable Integer anio,
            @RequestParam String tipoPrep) {
        try {
            List<IndicadorFechaDataProjection> datos = reporteUtSequiaService.getAllDataFecha(anio, tipoPrep);
            return ResponseEntity.ok(datos);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Get all Unidad Territorial data for a specific demarcation and year, prepTipo can be "prep1" or "prep3"
    @GetMapping("/demarcacion/{demarcacionId}/anio/{anio}/{prepTipo}")
    public ResponseEntity<?> getDataFechaDemarcacion(
            @PathVariable Integer demarcacionId,
            @PathVariable Integer anio,
            @PathVariable String prepTipo) {
        try {
            List<IndicadorDemarcacionFechaDataProjection> datos = reporteUtSequiaService.getAllDataFechaDemarcacion(demarcacionId, anio, prepTipo);
            return ResponseEntity.ok(datos);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/total/{utId}/anio/{anio}")
    public ResponseEntity<?> getTotalDataUTFecha(
            @PathVariable Integer utId,
            @PathVariable Integer anio) {
        try {
            List<IndicadorUTFechaDataProjection> datos = reporteUtSequiaService.getTotalDataUTFecha(utId, anio);
            return ResponseEntity.ok(datos);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/ut-estacion/{utId}/anio/{anio}")
    public ResponseEntity<?> getUTEstacionFecha(
            @PathVariable Integer utId,
            @PathVariable Integer anio) {
        try {
            List<IndicadorUTFechaDataProjection> datos = reporteUtSequiaService.getUTEstacionFecha(utId, anio);
            return ResponseEntity.ok(datos);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/reporte-word/{tipo}/{anio}/{mes}")
    public ResponseEntity<?> generarReporteWord(
            @PathVariable Integer anio,
            @PathVariable Integer mes,
            @PathVariable String tipo) {
        try {
            if (!tipo.equals("oriental") && !tipo.equals("occidental")) {
                return ResponseEntity.badRequest().body("El tipo debe ser 'oriental' u 'occidental'");
            }

            reporteWordUtSequiaService.generarReporteWord(anio, mes, tipo);
            return ResponseEntity.ok("Reporte generado exitosamente");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}