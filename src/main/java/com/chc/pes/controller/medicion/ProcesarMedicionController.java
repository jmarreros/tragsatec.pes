package com.chc.pes.controller.medicion;

import com.chc.pes.exception.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.chc.pes.dto.medicion.MedicionManualDTO;
import com.chc.pes.service.medicion.ProcesarMedicionService;

@RestController
@RequestMapping("/api/v1/procesar-medicion") // Ajusta la ruta base según tus convenciones
@RequiredArgsConstructor
public class ProcesarMedicionController {

    private final ProcesarMedicionService procesarMedicionService;

    @PostMapping("/manual")
    public ResponseEntity<String> procesarMedicionManual(
            @RequestBody MedicionManualDTO request) {
        try {
            procesarMedicionService.procesarMedicionManual(
                    request.getTipo(),
                    request.getAnio(),
                    request.getMes(),
                    request.getDetallesMedicion()
            );
            return ResponseEntity.ok("Medición manual guardada correctamente.");
        } catch (MedicionValidationException | PesNoValidoException | ArchivoValidationException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Error en los datos proporcionados: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al procesar la medición manual: " + e.getMessage());
        }
    }

    @PostMapping("/upload") // Para subir un archivo de mediciones
    public ResponseEntity<String> procesarArchivoMedicion(
            @RequestParam("tipo") Character tipo,
            @RequestParam("anio") Short anio,
            @RequestParam("mes") Byte mes,
            @RequestParam("file") MultipartFile file) {

        try {
            // Procesar el archivo
            procesarMedicionService.procesarArchivoMedicion(tipo, anio, mes, file);

            return ResponseEntity.ok("Mediciones guardadas correctamente.");

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