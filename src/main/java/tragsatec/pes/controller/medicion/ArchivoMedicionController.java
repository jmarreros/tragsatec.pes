package tragsatec.pes.controller.medicion;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tragsatec.pes.dto.medicion.ArchivoMedicionDTO;
import tragsatec.pes.exception.ArchivoMuyGrandeException;
import tragsatec.pes.exception.ArchivoValidationException;
import tragsatec.pes.exception.TipoArchivoNoSoportadoException;
import tragsatec.pes.service.medicion.ArchivoMedicionService;
import tragsatec.pes.service.medicion.ValidacionArchivoService;

import java.util.List;

@RestController
@RequestMapping("archivo-medicion")
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
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (ArchivoValidationException e) { // Captura genérica
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al subir el archivo: " + e.getMessage());
        }
    }
}