package tragsatec.pes.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import tragsatec.pes.persistence.entity.FileMeasurement;
import tragsatec.pes.service.FileStorageService;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/files")
public class FileUploadController {

    private final FileStorageService fileStorageService;

    @Value("${file.max-size}")
    private String maxFileSize;

    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList(".xls", ".xlsx", ".csv");
    private static final List<String> ALLOWED_CONTENT_TYPES = Arrays.asList(
            "application/vnd.ms-excel", // for .xls files
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", // for .xlsx files
            "text/csv" // for .csv files
    );

    public FileUploadController(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    private long parseSize(String size) {
        String lowerSize = size.toLowerCase();
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
        return Long.parseLong(lowerSize) * multiplier;
    }

    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file,
                                        @RequestParam("month") byte month,
                                        @RequestParam("year") short year) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("The file is empty.");
        }

        long maxFileSizeBytes = parseSize(maxFileSize);

        // File size validation
        if (file.getSize() > maxFileSizeBytes) {
            return ResponseEntity.badRequest().body("The file exceeds the maximum allowed size of " + maxFileSize + ".");
        }

        // File type validation (extension and content type)
        String originalFilename = file.getOriginalFilename();
        String fileExtension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            fileExtension = originalFilename.substring(originalFilename.lastIndexOf('.')).toLowerCase();
        }

        if (!ALLOWED_EXTENSIONS.contains(fileExtension) || !ALLOWED_CONTENT_TYPES.contains(file.getContentType())) {
            return ResponseEntity.badRequest().body("File type not allowed. Only Excel (.xls, .xlsx) or CSV (.csv) files are allowed.");
        }

        try {
            FileMeasurement storedFile = fileStorageService.storeFile(file, month, year);
            return ResponseEntity.status(HttpStatus.CREATED).body(storedFile);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error uploading file: " + e.getMessage());
        }
    }
}