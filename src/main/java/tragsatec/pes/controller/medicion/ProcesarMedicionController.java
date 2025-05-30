package tragsatec.pes.controller.medicion;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import tragsatec.pes.exception.ArchivoMuyGrandeException;
import tragsatec.pes.exception.ArchivoValidationException;
import tragsatec.pes.exception.TipoArchivoNoSoportadoException;
import tragsatec.pes.service.medicion.ProcesarMedicionService;
import tragsatec.pes.service.medicion.ValidacionArchivoService;

@RestController
@RequestMapping("/procesar-medicion") // Ajusta la ruta base según tus convenciones
@RequiredArgsConstructor
public class ProcesarMedicionController {

    private final ProcesarMedicionService procesarMedicionService;
    private final ValidacionArchivoService validacionArchivoService; // Inyectar el servicio

    @PostMapping("/upload")
    public ResponseEntity<String> procesarArchivoMedicion(
            @RequestParam("tipo") Character tipo,
            @RequestParam("anio") Short anio,
            @RequestParam("mes") Byte mes,
            @RequestParam("file") MultipartFile file) {

        // Validaciones de parámetros específicos del endpoint
        if (tipo != 'E' && tipo != 'S') {
            return ResponseEntity.badRequest().body("El campo 'tipo' debe ser 'E' o 'S'.");
        }
        // Aquí podrías añadir más validaciones para año y mes si es necesario

        try {
            // 1. Validar el archivo
            validacionArchivoService.validarArchivo(file);

            // 2. Llama al servicio para procesar el archivo
            // Este método debe ser implementado en ProcesarMedicionService
//            procesarMedicionService.procesarArchivoMedicion(tipo, anio, mes, file);

            return ResponseEntity.ok("Archivo procesado exitosamente.");

        } catch (ArchivoMuyGrandeException | TipoArchivoNoSoportadoException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (ArchivoValidationException e) { // Captura genérica
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (IllegalArgumentException e) { // Para errores de parámetros como 'tipo', 'anio', 'mes'
            return ResponseEntity.badRequest().body("Error en los datos proporcionados: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al procesar el archivo: " + e.getMessage());
        }
    }
}