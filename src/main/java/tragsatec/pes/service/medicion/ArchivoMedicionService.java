package tragsatec.pes.service.medicion;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import tragsatec.pes.dto.medicion.ArchivoMedicionDTO;
import tragsatec.pes.persistence.entity.medicion.ArchivoMedicionEntity;
import tragsatec.pes.persistence.entity.medicion.MedicionEntity;
import tragsatec.pes.persistence.repository.medicion.ArchivoMedicionRepository;
import tragsatec.pes.persistence.repository.medicion.MedicionRepository;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ArchivoMedicionService {

    private final ArchivoMedicionRepository archivoMedicionRepository;
    private final MedicionRepository medicionRepository;
    private final Path fileStorageLocation;

    public static final List<String> ALLOWED_EXTENSIONS = Arrays.asList(".xls", ".xlsx", ".csv");
    public static final List<String> ALLOWED_CONTENT_TYPES = Arrays.asList(
            "application/vnd.ms-excel",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "text/csv"
    );

    public ArchivoMedicionService(ArchivoMedicionRepository archivoMedicionRepository,
                                  MedicionRepository medicionRepository,
                                  @Value("${file.upload-dir}") String uploadDir) {
        this.archivoMedicionRepository = archivoMedicionRepository;
        this.medicionRepository = medicionRepository;
        this.fileStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new RuntimeException("No se pudo crear el directorio donde se almacenarán los archivos subidos.", ex);
        }
    }

    private ArchivoMedicionDTO mapToDTO(ArchivoMedicionEntity entity) {
        if (entity == null) return null;
        ArchivoMedicionDTO dto = new ArchivoMedicionDTO();
        dto.setId(entity.getId());
        dto.setFilePath(entity.getFilePath());
        dto.setFileName(entity.getFileName());
        if (entity.getMedicion() != null) {
            dto.setMedicionId(entity.getMedicion().getId());
        }
        return dto;
    }

    private void mapToEntity(ArchivoMedicionDTO dto, ArchivoMedicionEntity entity) {
        if (dto.getMedicionId() != null) {
            MedicionEntity medicion = medicionRepository.findById(dto.getMedicionId())
                    .orElseThrow(() -> new EntityNotFoundException("Medición no encontrada con id: " + dto.getMedicionId()));
            entity.setMedicion(medicion);
        } else if (entity.getMedicion() == null) {
            throw new IllegalArgumentException("medicionId no puede ser nulo para ArchivoMedicion si no está ya establecida.");
        }
    }

    public List<ArchivoMedicionDTO> findAll() {
        return archivoMedicionRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public Optional<ArchivoMedicionDTO> findById(Integer id) {
        return archivoMedicionRepository.findById(id)
                .map(this::mapToDTO);
    }

    @Transactional
    public ArchivoMedicionDTO storeFile(MultipartFile file, Integer medicionId) {
        String originalFileName = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
        String uniqueFileName = generateUniqueFileName(originalFileName);
        Path targetLocation = this.fileStorageLocation.resolve(uniqueFileName);

        try {
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            MedicionEntity medicion = medicionRepository.findById(medicionId)
                    .orElseThrow(() -> new EntityNotFoundException("Medición no encontrada con ID: " + medicionId));

            ArchivoMedicionEntity archivoMedicion = new ArchivoMedicionEntity();
            archivoMedicion.setFileName(originalFileName);
            archivoMedicion.setFilePath(targetLocation.toString());
            archivoMedicion.setMedicion(medicion);

            ArchivoMedicionEntity savedFile = archivoMedicionRepository.save(archivoMedicion);
            return mapToDTO(savedFile);
        } catch (IOException ex) {
            throw new RuntimeException("No se pudo almacenar el archivo " + originalFileName + ". Por favor, inténtalo de nuevo.", ex);
        }
    }

    private String generateUniqueFileName(String originalFileName) {
        String baseName = StringUtils.stripFilenameExtension(originalFileName);
        String extension = StringUtils.getFilenameExtension(originalFileName);
        String uniqueFileName = originalFileName;
        Path targetPath = this.fileStorageLocation.resolve(uniqueFileName);
        int count = 0;
        while (Files.exists(targetPath)) {
            count++;
            uniqueFileName = baseName + "_" + count + "." + extension;
            targetPath = this.fileStorageLocation.resolve(uniqueFileName);
        }
        return uniqueFileName;
    }

    public Resource loadFileAsResource(Integer fileId) {
        try {
            // 1. Busca la entidad del archivo en la BD
            ArchivoMedicionEntity fileEntity = archivoMedicionRepository.findById(fileId)
                    .orElseThrow(() -> new EntityNotFoundException("Archivo no encontrado con ID: " + fileId));

            // 2. Construye la ruta al archivo físico
            Path filePath = Paths.get(fileEntity.getFilePath()).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new RuntimeException("No se pudo leer el archivo: " + fileEntity.getFileName());
            }
        } catch (MalformedURLException ex) {
            throw new RuntimeException("Error al construir la ruta del archivo.", ex);
        }
    }

    // Necesitas un método para obtener el nombre del archivo
    public String getFileName(Integer fileId) {
        return archivoMedicionRepository.findById(fileId)
                .map(ArchivoMedicionEntity::getFileName)
                .orElseThrow(() -> new EntityNotFoundException("Archivo no encontrado con ID: " + fileId));
    }
}