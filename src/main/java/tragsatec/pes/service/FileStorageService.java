package tragsatec.pes.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import tragsatec.pes.persistence.entity.FileMeasurement;
import tragsatec.pes.persistence.repository.FileMeasurementRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Objects;

@Service
public class FileStorageService {

    private final Path fileStorageLocation;
    private final FileMeasurementRepository fileMeasurementRepository;

    public FileStorageService(@Value("${file.upload-dir:files_measurement}") String uploadDir,
                              FileMeasurementRepository fileMeasurementRepository) {
        this.fileStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
        this.fileMeasurementRepository = fileMeasurementRepository;

        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new RuntimeException("Could not create the directory where the uploaded files will be stored.", ex);
        }
    }

    public FileMeasurement storeFile(MultipartFile file, byte month, short year) {
        String originalFileName = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
        String fileName = originalFileName;
        Path targetLocation = this.fileStorageLocation.resolve(fileName);
        int count = 0;
        // Avoid overwriting files with the same name
        while (Files.exists(targetLocation)) {
            count++;
            String baseName = originalFileName.substring(0, originalFileName.lastIndexOf('.'));
            String extension = originalFileName.substring(originalFileName.lastIndexOf('.'));
            fileName = baseName + "_" + count + extension;
            targetLocation = this.fileStorageLocation.resolve(fileName);
        }

        try {
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            FileMeasurement fileMeasurement = new FileMeasurement();
            fileMeasurement.setFileName(fileName);
            fileMeasurement.setFilePath(targetLocation.toString());
            fileMeasurement.setMonth(month);
            fileMeasurement.setYear(year);

            return fileMeasurementRepository.save(fileMeasurement);
        } catch (IOException ex) {
            throw new RuntimeException("Could not store file " + fileName + ". Please try again!", ex);
        }
    }
}