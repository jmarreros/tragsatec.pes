package com.chc.pes.service.medicion;

import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.chc.pes.dto.general.EstacionProjection;
import com.chc.pes.dto.medicion.DetalleMedicionDTO;
import com.chc.pes.dto.medicion.MedicionDTO;
import com.chc.pes.dto.medicion.MedicionDatoDTO;
import com.chc.pes.exception.ArchivoValidationException;
import com.chc.pes.exception.MedicionValidationException;
import com.chc.pes.exception.PesNoValidoException;
import com.chc.pes.service.estructura.PesService;
import com.chc.pes.service.estructura.PesUtEstacionService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import com.chc.pes.dto.medicion.PrevisualizacionFTPDTO;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProcesarMedicionService {

    private static final Logger logger = LoggerFactory.getLogger(ProcesarMedicionService.class);

    private final PesService pesService;
    private final ValidacionArchivoService validacionArchivoService;
    private final PesUtEstacionService pesUtEstacionService;
    private final MedicionService medicionService;
    private final ArchivoMedicionService archivoMedicionService;
    private final FTPService ftpService;

    // Procesa una medición manual con los detalles proporcionados
    @Transactional
    public void procesarMedicionManual(Character tipo, Short anio, Byte mes, List<DetalleMedicionDTO> detallesMedicion) {

        // 1- Validar los parámetros de entrada
        validarTipoMedicion(tipo);
        ValidarNextYearAndMonth(anio, mes, tipo);

        // 2- Detectar el Plan Especial de Sequia (PES) actual
        Integer pesId = pesService.findActiveAndApprovedPesId().orElseThrow(
                () -> new PesNoValidoException("No hay un Plan Especial de Sequía (PES) activo y aprobado.")
        );

        // 3- Validar que los detalles de medición no estén vacíos
        if (detallesMedicion == null || detallesMedicion.isEmpty()) {
            throw new MedicionValidationException("Los detalles de medición no pueden estar vacíos.");
        }

        // 4- Validar que las estaciones del detalle existan en el PES actual
        Map<Integer, String> estacionesPorId = pesUtEstacionService.getEstacionesByPesId(pesId, tipo)
                .stream()
                .collect(Collectors.toMap(
                        EstacionProjection::getId,
                        EstacionProjection::getCodigo
                ));

        for (DetalleMedicionDTO detalle : detallesMedicion) {
            if (!estacionesPorId.containsKey(detalle.getEstacionId())) {
                throw new ArchivoValidationException("La estación con ID '" + detalle.getEstacionId() + "' no está registrada en el PES actual.");
            }
        }

        // 5- Anular la medición anterior para el PES, tipo, anio y mes
        medicionService.anularMedicionAnterior(pesId, tipo, anio, mes);

        // 6- Crear la nueva medición con todos sus detalles
        MedicionDTO nuevaMedicion = new MedicionDTO();
        nuevaMedicion.setPesId(pesId);
        nuevaMedicion.setTipo(tipo);
        nuevaMedicion.setAnio(anio);
        nuevaMedicion.setMes(mes);
        nuevaMedicion.setEliminado(false);

        // Crear los detalles de la medición
        java.util.Set<DetalleMedicionDTO> detallesSet = new java.util.HashSet<>(detallesMedicion);
        nuevaMedicion.setDetallesMedicion(detallesSet);

        // 7- Guardar la medición con todos sus detalles en una sola operación
        medicionService.save(nuevaMedicion);
    }

    // Procesa una medición a partir de un archivo cargado
    @Transactional
    public void procesarArchivoMedicion(Character tipo, Short anio, Byte mes, MultipartFile file) {

        // 1- Validar los parámetros de entrada
        validarTipoMedicion(tipo);
        ValidarNextYearAndMonth(anio, mes, tipo);
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
            if (!estacionesPorCodigo.containsKey(codigoEstacion.toUpperCase())) {
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

            detalle.setEstacionId(estacionesPorCodigo.get(dato.getNombreEstacion().toUpperCase()));
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

    private void ValidarNextYearAndMonth(Short anio, Byte mes, Character tipo) {
        if (anio == null || mes == null) {
            throw new IllegalArgumentException("El año y el mes no pueden ser nulos.");
        }

        Optional<MedicionDTO> ultimaMedicion = medicionService.findLastProcessedMedicionByTipo(tipo);
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

            throw new ArchivoValidationException("Tipo de archivo no soportado. Solo se permiten archivos CSV.");
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
                String[] values = line.split(",", -1); // -1 para no limitar el número de splits, permitiendo valores con comas vacíos

                if (values.length >= 2) { // Asegura que hay al menos nombre de estación y algo para el valor
                    String nombreEstacion = values[0].trim();
                    // Obtener el último valor de la fila (última columna)
                    final String valorNumericoOriginalStr = extraerValorUltimaColumna(values);

                    try {
                        // Limpiar comillas dobles y reemplazar la coma decimal por un punto
                        String valorLimpio = valorNumericoOriginalStr.replace("\"", "").replace(',', '.');

                        if (nombreEstacion.isEmpty() && !valorLimpio.isEmpty()) {
                            throw new ArchivoValidationException("Fila CSV con nombre de estación vacío pero con valor de medición '" + valorLimpio + "'. Línea: " + line);
                        }

                        // Si no hay valor, se omite la medicion para esa estación
                        if ( valorLimpio.isEmpty()) {
                         continue;
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

    private static String extraerValorUltimaColumna(String[] values) {
        String ultimoComponenteValor = values[values.length - 1].trim();
        String valorNumericoOriginalStr;

        // Comprobar si el valor numérico (que es el último campo lógico)
        // está entrecomillado y contiene una coma, lo que causaría que split(",") lo divida.
        // Ejemplo: "58,70" se divide en values[n-1]="\"58" y values[n]="70\""
        if (values.length > 2 && !ultimoComponenteValor.startsWith("\"") && ultimoComponenteValor.endsWith("\"") &&
                values[values.length - 2].trim().startsWith("\"") && !values[values.length - 2].trim().endsWith("\"")) {
            // Reconstruir el valor original que estaba entre comillas
            valorNumericoOriginalStr = values[values.length - 2].trim() + "," + ultimoComponenteValor;
        } else {
            // El valor está completamente en la última columna (puede o no estar entrecomillado)
            valorNumericoOriginalStr = ultimoComponenteValor;
        }
        return valorNumericoOriginalStr;
    }


    /**
     * Previsualiza los datos de un archivo de medición desde FTP sin procesarlos.
     * Descarga el archivo, lo valida y retorna los datos para revisión del usuario.
     *
     * @param tipo Tipo de medición ('S' para Sequía, 'E' para Escasez)
     * @return DTO con los datos de previsualización
     */
    public PrevisualizacionFTPDTO previsualizarDatosFTP(Character tipo) {
        // 1- Validar el parámetro de entrada
        validarTipoMedicion(tipo);

        // 2- Obtener la medición pendiente o nueva
        MedicionDTO medicionPendiente = medicionService.obtenerMedicionPendienteONueva(tipo);

        // 3- Definir el nombre del archivo a buscar en el servidor FTP/SFTP
        String nombreArchivoBase = construirNombreArchivoFTP(tipo, medicionPendiente.getAnio(), medicionPendiente.getMes());

        // 4- Buscar y copiar el archivo desde el servidor FTP/SFTP
        boolean archivoCopiado = ftpService.buscarYCopiarArchivo(nombreArchivoBase);
        if (!archivoCopiado) {
            throw new ArchivoValidationException("No se encontró el archivo '" + nombreArchivoBase + "' en el servidor FTP/SFTP.");
        }
        logger.info("Archivo '{}' copiado exitosamente desde el servidor FTP/SFTP.", nombreArchivoBase);

        // 5- Buscar el archivo más reciente (puede tener sufijo numérico si ya existía)
        String nombreArchivoReal = archivoMedicionService.buscarArchivoMasReciente(nombreArchivoBase);
        if (nombreArchivoReal == null) {
            throw new ArchivoValidationException("No se pudo encontrar el archivo descargado '" + nombreArchivoBase + "' en el directorio local.");
        }
        logger.info("Archivo más reciente encontrado: '{}'", nombreArchivoReal);

        // 6- Cargar el archivo copiado como MultipartFile
        MultipartFile archivoMedicion = archivoMedicionService.loadFileAsMultipart(nombreArchivoReal);

        // 7- Validar el archivo usando ValidacionArchivoService
        validacionArchivoService.validarArchivo(archivoMedicion);
        logger.info("Archivo '{}' validado correctamente.", nombreArchivoReal);

        // 8- Obtener los datos del archivo sin procesarlos
        List<MedicionDatoDTO> datosMedicion = get_datos_medicion(archivoMedicion);
        if (datosMedicion.isEmpty()) {
            throw new ArchivoValidationException("El archivo de medición está vacío o no contiene datos válidos.");
        }

        // 9- Crear y retornar el DTO de previsualización
        PrevisualizacionFTPDTO previsualizacion = new PrevisualizacionFTPDTO();
        previsualizacion.setAnio(medicionPendiente.getAnio());
        previsualizacion.setMes(medicionPendiente.getMes());
        previsualizacion.setTipo(tipo);
        previsualizacion.setNombreArchivo(nombreArchivoReal);
        previsualizacion.setDatos(datosMedicion);
        previsualizacion.setTotalRegistros(datosMedicion.size());

        return previsualizacion;
    }

    /**
     * Procesa el archivo de medición previamente descargado desde FTP.
     * Se asume que el archivo ya fue descargado y validado en la previsualización.
     *
     * @param tipo Tipo de medición ('S' para Sequía, 'E' para Escasez)
     */
    @Transactional
    public void procesarArchivoFTPDescargado(Character tipo) {
        // 1- Validar el parámetro de entrada
        validarTipoMedicion(tipo);

        // 2- Obtener la medición pendiente o nueva
        MedicionDTO medicionPendiente = medicionService.obtenerMedicionPendienteONueva(tipo);

        // 3- Definir el nombre base del archivo esperado
        String nombreArchivoBase = construirNombreArchivoFTP(tipo, medicionPendiente.getAnio(), medicionPendiente.getMes());

        // 4- Buscar el archivo más reciente (puede tener sufijo numérico)
        String nombreArchivoReal = archivoMedicionService.buscarArchivoMasReciente(nombreArchivoBase);
        if (nombreArchivoReal == null) {
            throw new ArchivoValidationException("El archivo '" + nombreArchivoBase + "' no ha sido descargado previamente. Por favor, primero previsualice los datos.");
        }
        logger.info("Archivo más reciente encontrado para procesar: '{}'", nombreArchivoReal);

        // 5- Cargar el archivo como MultipartFile
        MultipartFile archivoMedicion = archivoMedicionService.loadFileAsMultipart(nombreArchivoReal);

        // 6- Validar el archivo usando ValidacionArchivoService
        validacionArchivoService.validarArchivo(archivoMedicion);
        logger.info("Archivo '{}' validado correctamente para procesamiento.", nombreArchivoReal);

        // 7- Procesar el archivo de medición
        procesarArchivoMedicion(tipo, medicionPendiente.getAnio(), medicionPendiente.getMes(), archivoMedicion);

        logger.info("Archivo '{}' procesado exitosamente.", nombreArchivoReal);
    }

    /**
     * Construye el nombre del archivo FTP basado en el tipo, año y mes.
     */
    private String construirNombreArchivoFTP(Character tipo, Short anio, Byte mes) {
        String nombreMes = String.format("%02d", mes);
        String nombreAnio = String.valueOf(anio);
        String nombreTipo = tipo == 'S' ? "sequia" : "escasez";
        return nombreTipo + "_" + nombreMes + "_" + nombreAnio + ".csv";
    }


//    public void DescargarYProcesarMedicionesDesdeFTP(Character tipo) {
//        // 1- Validar el parámetro de entrada
//        validarTipoMedicion(tipo);
//
//        // 2- Obtener la medición pendiente o nueva
//        MedicionDTO medicionPendiente = medicionService.obtenerMedicionPendienteONueva(tipo);
//
//        // 3- Definir el nombre del archivo a buscar en el servidor FTP/SFTP
//        String nombreMes = String.format("%02d", medicionPendiente.getMes());
//        String nombreAnio = String.valueOf(medicionPendiente.getAnio());
//        String nombreTipo = tipo == 'S' ? "sequia" : "escasez";
//
//        String nombreArchivo = nombreTipo + "_" + nombreMes + "_" + nombreAnio + ".csv";
//
//        // 4- Buscar y copiar el archivo desde el servidor FTP/SFTP
//        boolean archivoCopiado = ftpService.buscarYCopiarArchivo(nombreArchivo);
//        if (!archivoCopiado) {
//            throw new ArchivoValidationException("No se encontró el archivo '" + nombreArchivo + "' en el servidor FTP/SFTP.");
//        }
//        logger.info("Archivo '{}' copiado exitosamente desde el servidor FTP/SFTP.", nombreArchivo);
//
//        // 5- Cargar el archivo copiado como MultipartFile
//        MultipartFile archivoMedicion = archivoMedicionService.loadFileAsMultipart(nombreArchivo);
//
//        // 6- Validar el archivo usando ValidacionArchivoService
//        validacionArchivoService.validarArchivo(archivoMedicion);
//        logger.info("Archivo '{}' validado correctamente.", nombreArchivo);
//
//        // 7- Procesar el archivo de medición
//        procesarArchivoMedicion(tipo, medicionPendiente.getAnio(), medicionPendiente.getMes(), archivoMedicion);
//    }

}