package com.chc.pes.controller.medicion;

import com.chc.pes.exception.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.chc.pes.dto.medicion.MedicionManualDTO;
import com.chc.pes.dto.medicion.PrevisualizacionFTPDTO;
import com.chc.pes.service.medicion.ProcesarMedicionService;

@Slf4j
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
            log.error("Error al procesar medición manual",e);
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("Error al procesar medición manual",e);
            return ResponseEntity.badRequest().body("Error en los datos proporcionados: " + e.getMessage());
        } catch (Exception e) {
            log.error("Error al procesar medición manual",e);
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
            log.error("Error al procesar archivo de mediciones",e);
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (ArchivoValidationException e) { // Captura genérica
            log.error("Error al procesar archivo de mediciones",e);
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (IllegalArgumentException e) { // Para errores de parámetros como 'tipo', 'anio', 'mes'
           log.error("Error al procesar archivo de mediciones",e);        
            return ResponseEntity.badRequest().body("Error en los datos proporcionados: " + e.getMessage());
        } catch (Exception e) {
            log.error("Error al procesar archivo de mediciones",e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al procesar el archivo: " + e.getMessage());
        }
    }

    /**
     * Endpoint para previsualizar los datos de un archivo de medición desde FTP.
     * Descarga el archivo del servidor FTP/SFTP y retorna los datos para revisión del usuario
     * sin procesarlos ni guardarlos en la base de datos.
     *
     * @param tipo Tipo de medición ('S' para Sequía, 'E' para Escasez)
     * @return DTO con los datos de previsualización
     */
    @GetMapping("/ftp/previsualizar")
    public ResponseEntity<?> previsualizarDatosFTP(@RequestParam("tipo") Character tipo) {
        try {
            PrevisualizacionFTPDTO previsualizacion = procesarMedicionService.previsualizarDatosFTP(tipo);
            return ResponseEntity.ok(previsualizacion);
        } catch (ArchivoValidationException e) {
            log.error("Error al previsualizar archivo de mediciones desde FTP/SFTP", e);
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("Error en los parámetros al previsualizar datos FTP", e);
            return ResponseEntity.badRequest().body("Error en los datos proporcionados: " + e.getMessage());
        } catch (Exception e) {
            log.error("Error al previsualizar archivo de mediciones desde FTP/SFTP", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al previsualizar las mediciones desde FTP: " + e.getMessage());
        }
    }

    /**
     * Endpoint para procesar el archivo de medición previamente descargado desde FTP.
     * Este endpoint debe llamarse después de haber previsualizado los datos con /ftp/previsualizar.
     *
     * @param tipo Tipo de medición ('S' para Sequía, 'E' para Escasez)
     * @return Mensaje de confirmación del procesamiento
     */
    @PostMapping("/ftp/procesar")
    public ResponseEntity<String> procesarArchivoFTPDescargado(@RequestParam("tipo") Character tipo) {
        try {
            procesarMedicionService.procesarArchivoFTPDescargado(tipo);
            return ResponseEntity.ok("Mediciones desde FTP procesadas correctamente.");
        } catch (ArchivoValidationException e) {
            log.error("Error al procesar archivo de mediciones desde FTP/SFTP", e);
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("Error en los parámetros al procesar datos FTP", e);
            return ResponseEntity.badRequest().body("Error en los datos proporcionados: " + e.getMessage());
        } catch (Exception e) {
            log.error("Error al procesar archivo de mediciones desde FTP/SFTP", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al procesar las mediciones desde FTP: " + e.getMessage());
        }
    }



    @GetMapping("/ftp")
    public ResponseEntity<String> DescargarYProcesarMedicionesDesdeFTP(
            @RequestParam("tipo") Character tipo) {
        try{
            procesarMedicionService.DescargarYProcesarMedicionesDesdeFTP(tipo);
            return ResponseEntity.ok("Mediciones desde FTP procesadas correctamente.");
        } catch (Exception e) {
            log.error("Error al procesar archivo de mediciones desde FTP/SFTP",e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al procesar las mediciones desde FTP: " + e.getMessage());
        }
    }


}