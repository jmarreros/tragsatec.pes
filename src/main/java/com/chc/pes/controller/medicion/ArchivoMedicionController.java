package com.chc.pes.controller.medicion;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.chc.pes.dto.medicion.ArchivoMedicionDTO;
import com.chc.pes.exception.ArchivoMuyGrandeException;
import com.chc.pes.exception.ArchivoValidationException;
import com.chc.pes.exception.TipoArchivoNoSoportadoException;
import com.chc.pes.service.medicion.ArchivoMedicionService;
import com.chc.pes.service.medicion.ValidacionArchivoService;

import ch.qos.logback.core.util.StringUtil;
import io.micrometer.common.util.StringUtils;

import java.io.IOException;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/archivo-medicion")
@RequiredArgsConstructor
public class ArchivoMedicionController {

    private final ArchivoMedicionService archivoMedicionService;
    private final ValidacionArchivoService validacionArchivoService;

    @GetMapping
    public ResponseEntity<List<ArchivoMedicionDTO>> getAll() {
        return ResponseEntity.ok(archivoMedicionService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ArchivoMedicionDTO> getById(@PathVariable Integer id) {
        return archivoMedicionService.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping()
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file, @RequestParam("medicionId") Integer medicionId) {
        try {
            validacionArchivoService.validarArchivo(file);

            ArchivoMedicionDTO storedFileDto = archivoMedicionService.storeFile(file, medicionId);
            return ResponseEntity.status(HttpStatus.CREATED).body(storedFileDto);

        } catch (ArchivoMuyGrandeException | TipoArchivoNoSoportadoException e) {
            log.error("Error al procesar archivo de mediciones",e);
            return ResponseEntity.badRequest().body("Error al procesar archivo de mediciones");
        } catch (ArchivoValidationException e) { // Captura genérica
            log.error("Error al procesar archivo de mediciones",e);
            return ResponseEntity.badRequest().body("Error al procesar archivo de mediciones");
        } catch (EntityNotFoundException e) {
            log.error("Error al procesar archivo de mediciones",e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Error al procesar archivo de mediciones");
        } catch (RuntimeException e) {
            log.error("Error al subir el archivo",e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al subir el archivo: ");
        }
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> downloadFile(@PathVariable Integer id, HttpServletRequest request) {
        try {
            // Carga el archivo como un recurso
            Resource resource = archivoMedicionService.loadFileAsResource(id);
            String originalFilename = archivoMedicionService.getFileName(id);

            // Intenta determinar el tipo de contenido del archivo
            String contentType = null;
            try {
                contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
            } catch (IOException ex) {

            }
            if (StringUtils.isEmpty(contentType))
                contentType = "application/octet-stream";
            return ResponseEntity.ok()
                    .contentType(org.springframework.http.MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + originalFilename + "\"")
                    .body(resource);

        } catch (EntityNotFoundException e) {
              log.error("Error al procesar descarga de archivos de mediciones",e);
            return ResponseEntity.notFound().build();
        } catch (RuntimeException e) {
              log.error("Error al procesar descarga de archivos de mediciones",e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}