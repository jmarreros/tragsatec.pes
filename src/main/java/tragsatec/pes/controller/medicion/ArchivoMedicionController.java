package tragsatec.pes.controller.medicion;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tragsatec.pes.dto.medicion.ArchivoMedicionDTO;
import tragsatec.pes.service.medicion.ArchivoMedicionService;

import java.util.List;

@RestController
@RequestMapping("archivo-medicion")
@RequiredArgsConstructor
public class ArchivoMedicionController {

    private final ArchivoMedicionService archivoMedicionService;

    @Value("${file.max-size}")
    private String maxFileSizeConfig;

    private long parseSize(String size) {
        String lowerSize = size.toLowerCase().trim();
        long multiplier = 1;
        if (lowerSize.endsWith("kb")) {
            multiplier = 1024;
            lowerSize = lowerSize.substring(0, lowerSize.length() - 2);
        } else if (lowerSize.endsWith("mb")) {
            multiplier = 1024 * 1024;
            lowerSize = lowerSize.substring(0, lowerSize.length() - 2);
        } else if (lowerSize.endsWith("gb")) {
            multiplier = 1024 * 1024 * 1024;
            lowerSize = lowerSize.substring(0, lowerSize.length() - 2);
        }
        try {
            return Long.parseLong(lowerSize.trim()) * multiplier;
        } catch (NumberFormatException e) {
            System.err.println("Error al parsear tamaño de archivo: '" + size + "'");
            throw new IllegalArgumentException("Formato de tamaño de archivo inválido: " + size);
        }
    }

    @GetMapping
    public ResponseEntity<List<ArchivoMedicionDTO>> getAll() {
        return ResponseEntity.ok(archivoMedicionService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ArchivoMedicionDTO> getById(@PathVariable Integer id) {
        return archivoMedicionService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping()
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file,
                                        @RequestParam("medicionId") Integer medicionId,
                                        @RequestParam(name = "activo", required = false) Boolean activo) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("El archivo está vacío.");
        }

        long maxFileSizeBytes;
        try {
            maxFileSizeBytes = parseSize(maxFileSizeConfig);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error de configuración del tamaño máximo de archivo: " + e.getMessage());
        }

        if (file.getSize() > maxFileSizeBytes) {
            return ResponseEntity.badRequest().body("El archivo excede el tamaño máximo permitido de " + maxFileSizeConfig + ".");
        }

        String originalFilename = file.getOriginalFilename();
        String fileExtension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            fileExtension = originalFilename.substring(originalFilename.lastIndexOf('.')).toLowerCase();
        }

        if (!ArchivoMedicionService.ALLOWED_EXTENSIONS.contains(fileExtension) ||
                (file.getContentType() != null && !ArchivoMedicionService.ALLOWED_CONTENT_TYPES.contains(file.getContentType().toLowerCase()))) {
            return ResponseEntity.badRequest().body("Tipo de archivo no permitido. Sólo se permiten archivos Excel (.xls, .xlsx) o CSV (.csv). Extension: '" + fileExtension + "', ContentType: '" + file.getContentType() + "'");
        }

        try {
            ArchivoMedicionDTO storedFileDto = archivoMedicionService.storeFile(file, medicionId, activo);
            return ResponseEntity.status(HttpStatus.CREATED).body(storedFileDto);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al subir el archivo: " + e.getMessage());
        }
    }
}