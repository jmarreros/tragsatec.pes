package com.chc.pes.controller.reporte;

import com.chc.pes.service.reporte.ReporteWordUtEscasezService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.chc.pes.dto.calculo.IndicadorDataProjection;
import com.chc.pes.dto.calculo.IndicadorDemarcacionFechaDataProjection;
import com.chc.pes.dto.calculo.IndicadorFechaDataProjection;
import com.chc.pes.dto.calculo.IndicadorUTFechaDataProjection;
import com.chc.pes.service.reporte.ReporteUtEscasezService;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@RestController
@RequestMapping("/api/v1/reportes/ut-escasez")
public class ReporteUtEscasezController {

    private final ReporteUtEscasezService reporteUtEscasezService;
    private final ReporteWordUtEscasezService reporteWordUtEscasezService;

    @Autowired
    public ReporteUtEscasezController(ReporteUtEscasezService reporteUtEscasezService, ReporteWordUtEscasezService reporteWordUtEscasezService) {
        this.reporteUtEscasezService = reporteUtEscasezService;
        this.reporteWordUtEscasezService = reporteWordUtEscasezService;
    }

    @GetMapping("/{utId}")
    public ResponseEntity<?> getDataIndicadorAnioMes(@PathVariable Integer utId) {
        try {
            List<IndicadorDataProjection> datos = reporteUtEscasezService.getAllDataIndicadorAnioMes(utId);
            return ResponseEntity.ok(datos);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/fecha/{anio}")
    public ResponseEntity<?> getDataFecha(@PathVariable Integer anio) {
        try {
            List<IndicadorFechaDataProjection> datos = reporteUtEscasezService.getAllDataFecha(anio);
            return ResponseEntity.ok(datos);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/demarcacion/{demarcacionId}/anio/{anio}")
    public ResponseEntity<?> getDataFechaDemarcacion(
            @PathVariable Integer demarcacionId,
            @PathVariable Integer anio) {
        try {
            List<IndicadorDemarcacionFechaDataProjection> datos = reporteUtEscasezService.getAllDataFechaDemarcacion(demarcacionId, anio);
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
            List<IndicadorUTFechaDataProjection> datos = reporteUtEscasezService.getTotalDataUTFecha(utId, anio);
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
            List<IndicadorUTFechaDataProjection> datos = reporteUtEscasezService.getUTEstacionFecha(utId, anio);
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

            reporteWordUtEscasezService.generarReporteWord(anio, mes, tipo);
            return ResponseEntity.ok().body("Reporte generado exitosamente.");

//            String rutaArchivoDescargar = reporteWordUtEscasezService.downloadReporteWord(anio, mes, tipo);
//
//            Path path = Paths.get(rutaArchivoDescargar);
//            Resource resource;
//            try {
//                resource = new UrlResource(path.toUri());
//            } catch (MalformedURLException e) {
//                throw new RuntimeException("Error al leer el archivo.", e);
//            }
//
//            if (!resource.exists() || !resource.isReadable()) {
//                throw new RuntimeException("No se pudo encontrar o leer el archivo: " + rutaArchivoDescargar);
//            }
//
//            String filename = path.getFileName().toString();
//            HttpHeaders headers = new HttpHeaders();
//            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"");
//
//            return ResponseEntity.ok()
//                    .headers(headers)
//                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.wordprocessingml.document"))
//                    .body(resource);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error al generar el reporte: " + e.getMessage());
        }
    }
}