package com.chc.pes.controller.reporte;

import com.chc.pes.service.reporte.ReporteWordUtSequiaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.chc.pes.dto.calculo.IndicadorDataProjection;
import com.chc.pes.dto.calculo.IndicadorDemarcacionFechaDataProjection;
import com.chc.pes.dto.calculo.IndicadorFechaDataProjection;
import com.chc.pes.dto.calculo.IndicadorUTFechaDataProjection;
import com.chc.pes.service.reporte.ReporteUtSequiaService;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
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

    @GetMapping("/reporte-pdf/{tipo}/{anio}/{mes}")
    public ResponseEntity<?> generarReportePDF(
            @PathVariable Integer anio,
            @PathVariable Integer mes,
            @PathVariable String tipo) {
        try {
            if (!tipo.equals("oriental") && !tipo.equals("occidental")) {
                return ResponseEntity.badRequest().body("El tipo debe ser 'oriental' u 'occidental'");
            }

            // El servicio debe devolver la ruta al archivo PDF
            String pathToDownload = reporteWordUtSequiaService.downloadReportePDF(anio, mes, tipo);

            Path path = Paths.get(pathToDownload);
            Resource resource;
            try {
                resource = new UrlResource(path.toUri());
            } catch (MalformedURLException e) {
                throw new RuntimeException("Error al leer el archivo.", e);
            }

            if (!resource.exists() || !resource.isReadable()) {
                throw new RuntimeException("No se pudo encontrar o leer el archivo: " + pathToDownload);
            }

            String filename = path.getFileName().toString();
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"");

            return ResponseEntity.ok()
                    .headers(headers)
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(resource);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error al generar el reporte: " + e.getMessage());
        }
    }
}