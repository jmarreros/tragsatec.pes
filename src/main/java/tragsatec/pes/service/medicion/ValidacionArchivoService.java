package tragsatec.pes.service.medicion;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import tragsatec.pes.exception.ArchivoMuyGrandeException;
import tragsatec.pes.exception.ArchivoValidationException;
import tragsatec.pes.exception.TipoArchivoNoSoportadoException;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Service
public class ValidacionArchivoService {

    @Value("${file.max-size}")
    private String maxFileSizeConfig;


    public static final Set<String> ALLOWED_EXTENSIONS = new HashSet<>(Arrays.asList(".xlsx", ".xls", ".csv"));
    public static final Set<String> ALLOWED_CONTENT_TYPES = new HashSet<>(Arrays.asList(
            "application/vnd.ms-excel", // .xls
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", // .xlsx
            "text/csv" // .csv
    ));

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
            throw new IllegalArgumentException("Formato de tamaño de archivo inválido en la configuración: " + size);
        }
    }

    public void validarArchivo(MultipartFile file) {
        if (file.isEmpty()) {
            throw new ArchivoValidationException("El archivo está vacío.");
        }

        long maxFileSizeBytes;
        try {
            maxFileSizeBytes = parseSize(maxFileSizeConfig);
        } catch (IllegalArgumentException e) {
            throw new ArchivoValidationException("Error de configuración del tamaño máximo de archivo: " + e.getMessage());
        }

        if (file.getSize() > maxFileSizeBytes) {
            throw new ArchivoMuyGrandeException("El archivo excede el tamaño máximo permitido de " + maxFileSizeConfig + ".");
        }

        String originalFilename = file.getOriginalFilename();
        String fileExtension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            fileExtension = originalFilename.substring(originalFilename.lastIndexOf('.')).toLowerCase();
        }

        String contentType = file.getContentType() != null ? file.getContentType().toLowerCase() : "";

        if (!ALLOWED_EXTENSIONS.contains(fileExtension) || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new TipoArchivoNoSoportadoException("Tipo de archivo no permitido. Sólo se permiten archivos Excel (.xls, .xlsx) o CSV (.csv). Extensión: '" + fileExtension + "', ContentType: '" + contentType + "'");
        }
    }
}