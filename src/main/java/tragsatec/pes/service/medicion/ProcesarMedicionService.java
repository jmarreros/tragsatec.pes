package tragsatec.pes.service.medicion;

import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import tragsatec.pes.dto.estructura.EstacionProjection;
import tragsatec.pes.dto.medicion.DetalleMedicionDTO;
import tragsatec.pes.dto.medicion.MedicionDTO;
import tragsatec.pes.dto.medicion.MedicionDatoDTO;
import tragsatec.pes.exception.ArchivoValidationException;
import tragsatec.pes.exception.PesNoValidoException;
import tragsatec.pes.service.estructura.PesService;
import tragsatec.pes.service.estructura.PesUtEstacionService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProcesarMedicionService {
    private final PesService pesService;
    private final ValidacionArchivoService validacionArchivoService;
    private final PesUtEstacionService pesUtEstacionService;
    private final MedicionService medicionService;
    private final ArchivoMedicionService archivoMedicionService;

    public void procesarArchivoMedicion(Character tipo, Short anio, Byte mes, MultipartFile file) {

        // 1- Validar los parámetros de entrada
        validarTipoMedicion(tipo);
        ValidarNextYearAndMonth(anio, mes);
        validacionArchivoService.validarArchivo(file);

        // 2- Detectar el Plan Especial de Sequia (PES) actual
        Integer pesId = pesService.findActiveAndApprovedPesId().orElseThrow(
                () -> new PesNoValidoException("No hay un Plan Especial de Sequía (PES) activo y aprobado.")
        );

        // 3- Obtener la data del archivo de medicion
        List<MedicionDatoDTO> datosMedicion = get_datos_medicion(file);
        if (datosMedicion.isEmpty()) {
            throw new ArchivoValidationException("El archivo de medición está vacío o no contiene datos válidos.");
        }

        // 4- Obtener estaciones del PES actual desde la BD
        Map<String, Integer> estacionesPorCodigo = pesUtEstacionService.getEstacionesByPesId(pesId, tipo)
                .stream()
                .collect(Collectors.toMap(
                        EstacionProjection::getCodigo,
                        EstacionProjection::getId
                ));

        // 5- Validar que las estaciones del archivo de medición existan en el PES actual
        for (MedicionDatoDTO dato : datosMedicion) {
            String codigoEstacion = dato.getNombreEstacion();
            if (!estacionesPorCodigo.containsKey(codigoEstacion)) {
                throw new ArchivoValidationException("La estación '" + codigoEstacion + "' no está registrada en el PES actual.");
            }
        }

        // 6- Anular la medición anterior para el PES, tipo, anio y mes
        medicionService.anularMedicionAnterior(pesId, tipo, anio, mes);

        // 7- Crear la nueva medición con todos sus detalles
        MedicionDTO nuevaMedicion = new MedicionDTO();
        nuevaMedicion.setPesId(pesId);
        nuevaMedicion.setTipo(tipo);
        nuevaMedicion.setAnio(anio);
        nuevaMedicion.setMes(mes);
        nuevaMedicion.setEliminado(false);

        // Crear los detalles de la medición
        java.util.Set<DetalleMedicionDTO> detalles = new java.util.HashSet<>();
        for (MedicionDatoDTO dato : datosMedicion) {
            DetalleMedicionDTO detalle = new DetalleMedicionDTO();

            detalle.setEstacionId(estacionesPorCodigo.get(dato.getNombreEstacion()));
            detalle.setValor(dato.getValorMedicion());
            detalles.add(detalle);
        }
        nuevaMedicion.setDetallesMedicion(detalles);

        // Guardar la medición con todos sus detalles en una sola operación
        MedicionDTO medicionGuardada = medicionService.save(nuevaMedicion);

        // 8- Guardar archivo de medición
        archivoMedicionService.storeFile(file, medicionGuardada.getId());
    }

    private void validarTipoMedicion(Character tipo) {
        if (tipo == null) {
            throw new IllegalArgumentException("El tipo de medición no puede ser nulo.");
        }

        if (tipo != 'S' && tipo != 'E') {
            throw new IllegalArgumentException("Tipo de medición inválido. Debe ser 'S' para Sequía o 'E' para Sequia.");
        }
    }

    private void ValidarNextYearAndMonth(Short anio, Byte mes) {
        if (anio == null || mes == null) {
            throw new IllegalArgumentException("El año y el mes no pueden ser nulos.");
        }

        Optional<MedicionDTO> ultimaMedicion = medicionService.findLastProcessedMedicionByTipo('S');
        if (ultimaMedicion.isEmpty()) return;

        MedicionDTO ultima = ultimaMedicion.get();
        if (ultima.getAnio() > anio || (ultima.getAnio().equals(anio) && ultima.getMes() >= mes)) {
            throw new IllegalArgumentException("El año y mes proporcionados deben ser posteriores al último procesado: " + ultima.getAnio() + "-" + ultima.getMes());
        }

        if (mes < 1 || mes > 12) {
            throw new IllegalArgumentException("El mes debe estar entre 1 y 12.");
        }

        int currentYear = java.time.LocalDate.now().getYear();
        if (anio < 1980 || anio > currentYear) {
            throw new IllegalArgumentException("El año debe estar entre 1980 y " + currentYear);
        }

        // Evaluar que debe ser el siguiente año y mes
        int ultimoAnio = ultima.getAnio();
        byte ultimoMes = ultima.getMes();

        int siguienteAnio = ultimoAnio;
        byte siguienteMes;

        if (ultimoMes == 12) {
            siguienteMes = 1;
            siguienteAnio++;
        } else {
            siguienteMes = (byte) (ultimoMes + 1);
        }

        if (anio != siguienteAnio || mes != siguienteMes) {
            throw new IllegalArgumentException("El año y mes proporcionados deben ser el siguiente al último procesado: " + siguienteAnio + "-" + siguienteMes);
        }
    }

    private List<MedicionDatoDTO> get_datos_medicion(MultipartFile file) {
        try {
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
        } catch (IOException e) {
            throw new RuntimeException("Error al leer el archivo de medición: " + e.getMessage(), e);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Error en el formato del archivo de medición: " + e.getMessage(), e);
        }
    }

    private List<MedicionDatoDTO> parseCsv(MultipartFile file) throws IOException {
        List<MedicionDatoDTO> datos = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String line;

            if (br.ready()) {
                br.readLine(); // Omitir la primera línea de encabezado
            }

            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }
                String[] values = line.split(",", -1); // -1 para no limitar el número de splits, permitiendo valores con comas vacios

                if (values.length >= 2) { // Asegura que hay al menos nombre de estación y algo para el valor
                    String nombreEstacion = values[0].trim();
                    String primerComponenteValor = values[1].trim();
                    String valorNumericoOriginalStr;

                    // Comprobar si el valor numérico (que es el segundo campo lógico)
                    // está entrecomillado y contiene una coma, lo que causaría que split(",") lo divida.
                    // Ejemplo: "58,70" se divide en values[1]="\"58" y values[2]="70\""
                    if (values.length > 2 && primerComponenteValor.startsWith("\"") && !primerComponenteValor.endsWith("\"") && // La primera parte no termina con comilla
                            values[2].trim().endsWith("\"")) {       // La segunda parte sí termina con comilla
                        // Reconstruir el valor original que estaba entre comillas
                        valorNumericoOriginalStr = primerComponenteValor + "," + values[2].trim();
                    } else {
                        // El valor está completamente en values[1] (puede o no estar entrecomillado)
                        valorNumericoOriginalStr = primerComponenteValor;
                    }

                    try {
                        // Limpiar comillas dobles y reemplazar la coma decimal por un punto
                        String valorLimpio = valorNumericoOriginalStr.replace("\"", "").replace(',', '.');

                        if (valorLimpio.isEmpty()) {
                            if (!nombreEstacion.isEmpty()) {
                                throw new ArchivoValidationException("Fila CSV con valor de medición vacío para la estación '" + nombreEstacion + "'. Línea: " + line);
                            } else {
                                continue;
                            }
                        } else {
                            if (nombreEstacion.isEmpty()) {
                                throw new ArchivoValidationException("Fila CSV con nombre de estación vacío pero con valor de medición '" + valorLimpio + "'. Línea: " + line);
                            }
                        }

                        BigDecimal valorMedicion = new BigDecimal(valorLimpio);
                        datos.add(new MedicionDatoDTO(nombreEstacion, valorMedicion));

                    } catch (NumberFormatException e) {
                        String valorProblematico = valorNumericoOriginalStr.length() > 50 ? valorNumericoOriginalStr.substring(0, 50) + "..." : valorNumericoOriginalStr;
                        throw new ArchivoValidationException("Formato CSV inválido. Error al parsear el valor numérico: '" + valorProblematico + "'. Asegúrese de que sea un número válido. Línea: " + line);
                    }
                } else if (!line.replace(",", "").trim().isEmpty()) {
                    throw new ArchivoValidationException("Formato CSV inválido. Cada línea debe contener al menos dos valores separados por comas. Línea: " + line);
                }
            }
        }
        return datos;
    }

    private List<MedicionDatoDTO> parseXlsx(MultipartFile file) throws IOException {
        List<MedicionDatoDTO> datos = new ArrayList<>();
        DataFormatter dataFormatter = new DataFormatter(); // Útil para obtener el valor de la celda como String

        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0); // Asume datos en la primera hoja

            // Omitir la primera línea de encabezado si existe
            int startRow = sheet.getFirstRowNum();
            if (startRow == 0 && sheet.getRow(0) != null) {
                startRow = 1;
            }

            for (int i = startRow; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue; // Omitir filas completamente vacías

                Cell cellEstacion = row.getCell(0, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL); // Primera columna
                Cell cellMedicion = row.getCell(1, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL); // Segunda columna

                String nombreEstacion = null;
                if (cellEstacion != null) {
                    nombreEstacion = dataFormatter.formatCellValue(cellEstacion).trim();
                }

                BigDecimal valorMedicion = null;
                if (cellMedicion != null) {
                    String cellValueStr = dataFormatter.formatCellValue(cellMedicion).trim().replace(',', '.');
                    if (!cellValueStr.isEmpty()) {
                        try {
                            valorMedicion = new BigDecimal(cellValueStr);
                        } catch (NumberFormatException e) {
                            throw new ArchivoValidationException("Formato XLSX inválido. Error al parsear el valor numérico: '" + cellValueStr + "'. Asegúrese de que sea un número válido en la fila: " + (row.getRowNum() + 1));
                        }
                    }
                }

                if (nombreEstacion != null && !nombreEstacion.isEmpty() && valorMedicion != null) {
                    datos.add(new MedicionDatoDTO(nombreEstacion, valorMedicion));
                } else if (nombreEstacion != null && !nombreEstacion.isEmpty() && cellMedicion != null) {
                    throw new ArchivoValidationException("Fila XLSX con nombre de estación '" + nombreEstacion + "' pero con valor de medición vacío en la fila: " + (row.getRowNum() + 1));
                }
            }
        }
        return datos;
    }
}