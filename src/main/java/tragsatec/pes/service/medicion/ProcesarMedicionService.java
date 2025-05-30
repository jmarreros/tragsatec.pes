package tragsatec.pes.service.medicion;

import lombok.RequiredArgsConstructor;
//import org.apache.poi.ss.usermodel.*;
//import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import tragsatec.pes.dto.medicion.MedicionDatoDTO;
import tragsatec.pes.exception.ArchivoValidationException;
import tragsatec.pes.exception.PesNoValidoException;
import tragsatec.pes.service.estructura.PesService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProcesarMedicionService {
    private final PesService pesService;
    private final ValidacionArchivoService validacionArchivoService; // Inyectar el servicio

    public void procesarArchivoMedicion(Character tipo, Short anio, Byte mes, MultipartFile file) {

        // 1- Validar el archivo
        validacionArchivoService.validarArchivo(file);

        // 2- Detectar el Plan Especial de Sequia (PES) actual
        Integer pesId = pesService.findActiveAndApprovedPesId()
                .orElseThrow(() -> new PesNoValidoException("No hay un Plan Especial de Sequía (PES) activo y aprobado."));

        // 3- Obtener la data del archivo de medicion
        List<MedicionDatoDTO> datosMedicion;


        try {
            datosMedicion = parseDatosDesdeArchivo(file);
        } catch (IOException e) {
            // Considera una excepción más específica de tu aplicación si es necesario
            throw new RuntimeException("Error al leer el archivo de medición: " + e.getMessage(), e);
        } catch (IllegalArgumentException e) {
            // Para errores de formato de archivo o contenido
            throw new RuntimeException("Error en el formato del archivo de medición: " + e.getMessage(), e);
        }

        if (datosMedicion.isEmpty()) {
            // Manejar el caso de archivo vacío o sin datos válidos
            System.out.println("El archivo de medición está vacío o no contiene datos válidos.");
            return;
        }

        // Ahora puedes procesar la lista de MedicionDato
        for (MedicionDatoDTO dato : datosMedicion) {
            System.out.println("Procesando: Estación - " + dato.getNombreEstacion() + ", Valor - " + dato.getValorMedicion() + " para PES ID: " + pesId);
            // Aquí iría la lógica para guardar estos datos, asociándolos con pesId, tipo, anio, mes
        }
        System.out.println("Archivo " + file.getOriginalFilename() + " procesado con " + datosMedicion.size() + " registros.");
    }

    private List<MedicionDatoDTO> parseDatosDesdeArchivo(MultipartFile file) throws IOException {
        String filename = file.getOriginalFilename();

        if (filename == null || filename.isEmpty()) {
            throw new ArchivoValidationException("El nombre del archivo no puede estar vacío.");
        }

        if (filename.toLowerCase().endsWith(".csv")) {
            return parseCsv(file);
        }
        if (filename.toLowerCase().endsWith(".xlsx")) {
            return parseXlsx(file);
        }

        throw new ArchivoValidationException("Tipo de archivo no soportado. Solo se permiten archivos CSV o XLSX.");
    }

    private List<MedicionDatoDTO> parseCsv(MultipartFile file) throws IOException {
        List<MedicionDatoDTO> datos = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String line;

            if (br.ready()) br.readLine(); // Omitir la primera línea de encabezado

            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue; // Omitir líneas vacías
                String[] values = line.split(",");
                if (values.length >= 2) {
                    String nombreEstacion = values[0].trim();
                    try {
                        // Convertir el String a BigDecimal
                        BigDecimal valorMedicion = new BigDecimal(values[1].trim().replace(',', '.'));
                        if (!nombreEstacion.isEmpty()) { // Asegurarse que el nombre de la estación no esté vacío
                            datos.add(new MedicionDatoDTO(nombreEstacion, valorMedicion));
                        }
                    } catch (NumberFormatException e) {
                        throw new ArchivoValidationException("Formato CSV inválido. Error al parsear el valor numérico: '" + values[1].trim() + "'. Asegúrese de que sea un número válido.");
                    }
                } else {
                    throw new ArchivoValidationException("Formato CSV inválido. Cada línea debe contener al menos dos valores separados por comas.");
                }
            }
        }
        return datos;
    }

    private List<MedicionDatoDTO> parseXlsx(MultipartFile file) throws IOException {
        List<MedicionDatoDTO> datos = new ArrayList<>();
//        DataFormatter dataFormatter = new DataFormatter(); // Útil para obtener el valor de la celda como String
//
//        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
//            Sheet sheet = workbook.getSheetAt(0); // Asume datos en la primera hoja
//            // Omitir la primera línea de encabezado si existe
//            int startRow = sheet.getFirstRowNum();
//            if (startRow == 0 && sheet.getRow(0) != null) { // Podrías añadir una lógica más robusta para detectar encabezados
//                startRow = 1;
//            }
//
//
//            for (int i = startRow; i <= sheet.getLastRowNum(); i++) {
//                Row row = sheet.getRow(i);
//                if (row == null) continue; // Omitir filas completamente vacías
//
//                Cell cellEstacion = row.getCell(0, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL); // Primera columna
//                Cell cellMedicion = row.getCell(1, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL); // Segunda columna
//
//                String nombreEstacion = null;
//                if (cellEstacion != null) {
//                    nombreEstacion = dataFormatter.formatCellValue(cellEstacion).trim();
//                }
//
//                BigDecimal valorMedicion = null;
//                if (cellMedicion != null) {
//                    String cellValueStr = dataFormatter.formatCellValue(cellMedicion).trim().replace(',', '.');
//                    if (!cellValueStr.isEmpty()) {
//                        try {
//                            valorMedicion = new BigDecimal(cellValueStr);
//                        } catch (NumberFormatException e) {
//                            System.err.println("Advertencia: Celda XLSX omitida. Error al parsear valor numérico '" + cellValueStr + "' en fila: " + (row.getRowNum() + 1));
//                        }
//                    }
//                }
//
//                if (nombreEstacion != null && !nombreEstacion.isEmpty() && valorMedicion != null) {
//                    datos.add(new MedicionDatoDTO(nombreEstacion, valorMedicion));
//                } else if (nombreEstacion != null && !nombreEstacion.isEmpty() && valorMedicion == null && cellMedicion != null) {
//                    // Si hay nombre de estación pero el valor de medición no se pudo parsear y la celda no era nula
//                    System.err.println("Advertencia: Fila XLSX parcialmente omitida. Nombre estación: '" + nombreEstacion + "', valor medición no válido en fila: " + (row.getRowNum() + 1));
//                }
//            }
//        }
        return datos;
    }
}