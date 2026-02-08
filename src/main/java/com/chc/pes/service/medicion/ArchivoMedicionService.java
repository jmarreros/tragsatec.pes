package com.chc.pes.service.medicion;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import com.chc.pes.dto.medicion.ArchivoMedicionDTO;
import com.chc.pes.persistence.entity.medicion.ArchivoMedicionEntity;
import com.chc.pes.persistence.entity.medicion.MedicionEntity;
import com.chc.pes.persistence.repository.medicion.ArchivoMedicionRepository;
import com.chc.pes.persistence.repository.medicion.MedicionRepository;

import java.io.IOException;
import java.io.InputStream;
import java.io.File;
import java.io.ByteArrayInputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ArchivoMedicionService {

    private final ArchivoMedicionRepository archivoMedicionRepository;
    private final MedicionRepository medicionRepository;
    private final Path fileStorageLocation;
    private final Path temporalStorageLocation;

    public ArchivoMedicionService(ArchivoMedicionRepository archivoMedicionRepository,
                                  MedicionRepository medicionRepository,
                                  @Value("${file.upload-dir}") String uploadDir,
                                  @Value("${file.temporal-dir}") String temporalDir) {
        this.archivoMedicionRepository = archivoMedicionRepository;
        this.medicionRepository = medicionRepository;
        this.fileStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
        this.temporalStorageLocation = Paths.get(temporalDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.fileStorageLocation);
            Files.createDirectories(this.temporalStorageLocation);
        } catch (Exception ex) {
            throw new RuntimeException("No se pudo crear los directorios de almacenamiento.", ex);
        }
    }

    private ArchivoMedicionDTO mapToDTO(ArchivoMedicionEntity entity) {
        if (entity == null) return null;
        ArchivoMedicionDTO dto = new ArchivoMedicionDTO();
        dto.setId(entity.getId());
        dto.setFileName(entity.getFileName());
        if (entity.getMedicion() != null) {
            dto.setMedicionId(entity.getMedicion().getId());
        }
        return dto;
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

        Path targetLocation = this.fileStorageLocation.resolve(originalFileName);

        try {
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            MedicionEntity medicion = medicionRepository.findById(medicionId)
                    .orElseThrow(() -> new EntityNotFoundException("Medición no encontrada con ID: " + medicionId));

            ArchivoMedicionEntity archivoMedicion = new ArchivoMedicionEntity();
            archivoMedicion.setFileName(originalFileName);
            archivoMedicion.setMedicion(medicion);

            ArchivoMedicionEntity savedFile = archivoMedicionRepository.save(archivoMedicion);
            return mapToDTO(savedFile);
        } catch (IOException ex) {
            throw new RuntimeException("No se pudo almacenar el archivo " + originalFileName + ". Por favor, inténtalo de nuevo.", ex);
        }
    }

    public Resource loadFileAsResource(Integer fileId) {
        try {
            // 1. Busca la entidad del archivo en la BD
            ArchivoMedicionEntity fileEntity = archivoMedicionRepository.findById(fileId)
                    .orElseThrow(() -> new EntityNotFoundException("Archivo no encontrado con ID: " + fileId));

            // 2. Construye la ruta al archivo físico usando fileStorageLocation y el nombre del archivo de la entidad.
            Path filePath = this.fileStorageLocation.resolve(fileEntity.getFileName()).normalize();
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

    // Para obtener el nombre del archivo
    public String getFileName(Integer fileId) {
        return archivoMedicionRepository.findById(fileId)
                .map(ArchivoMedicionEntity::getFileName)
                .orElseThrow(() -> new EntityNotFoundException("Archivo no encontrado con ID: " + fileId));
    }

    /**
     * Carga un archivo desde el directorio de uploads como MultipartFile
     *
     * @param fileName nombre del archivo a cargar
     * @return MultipartFile con el contenido del archivo
     */
    public MultipartFile loadFileAsMultipart(String fileName) {
        try {
            Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
            File file = filePath.toFile();

            if (!file.exists() || !file.canRead()) {
                throw new RuntimeException("No se pudo leer el archivo: " + fileName);
            }

            byte[] content = Files.readAllBytes(filePath);
            String contentType = Files.probeContentType(filePath);
            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            final String finalContentType = contentType;
            final String finalFileName = fileName;

            return new MultipartFile() {
                @Override
                public String getName() {
                    return finalFileName;
                }

                @Override
                public String getOriginalFilename() {
                    return finalFileName;
                }

                @Override
                public String getContentType() {
                    return finalContentType;
                }

                @Override
                public boolean isEmpty() {
                    return content.length == 0;
                }

                @Override
                public long getSize() {
                    return content.length;
                }

                @Override
                public byte[] getBytes() {
                    return content;
                }

                @Override
                public InputStream getInputStream() {
                    return new ByteArrayInputStream(content);
                }

                @Override
                public void transferTo(File dest) throws IOException {
                    Files.write(dest.toPath(), content);
                }
            };
        } catch (IOException ex) {
            throw new RuntimeException("Error al cargar el archivo " + fileName + " como MultipartFile.", ex);
        }
    }




    /**
     * Carga un archivo desde el directorio temporal como MultipartFile.
     *
     * @param fileName nombre del archivo a cargar
     * @return MultipartFile con el contenido del archivo
     */
    public MultipartFile loadFileFromTemporalAsMultipart(String fileName) {
        return loadFileFromPathAsMultipart(fileName, this.temporalStorageLocation);
    }

    /**
     * Verifica si un archivo existe en el directorio temporal.
     *
     * @param fileName nombre del archivo a verificar
     * @return true si el archivo existe y es legible, false en caso contrario
     */
    public boolean existeArchivoEnTemporal(String fileName) {
        Path filePath = this.temporalStorageLocation.resolve(fileName).normalize();
        File file = filePath.toFile();
        return file.exists() && file.canRead();
    }

    /**
     * Carga un archivo desde una ruta específica como MultipartFile.
     */
    private MultipartFile loadFileFromPathAsMultipart(String fileName, Path storagePath) {
        try {
            Path filePath = storagePath.resolve(fileName).normalize();
            File file = filePath.toFile();

            if (!file.exists() || !file.canRead()) {
                throw new RuntimeException("No se pudo leer el archivo: " + fileName);
            }

            byte[] content = Files.readAllBytes(filePath);
            String contentType = Files.probeContentType(filePath);
            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            final String finalContentType = contentType;
            final String finalFileName = fileName;

            return new MultipartFile() {
                @Override
                public String getName() {
                    return finalFileName;
                }

                @Override
                public String getOriginalFilename() {
                    return finalFileName;
                }

                @Override
                public String getContentType() {
                    return finalContentType;
                }

                @Override
                public boolean isEmpty() {
                    return content.length == 0;
                }

                @Override
                public long getSize() {
                    return content.length;
                }

                @Override
                public byte[] getBytes() {
                    return content;
                }

                @Override
                public InputStream getInputStream() {
                    return new ByteArrayInputStream(content);
                }

                @Override
                public void transferTo(File dest) throws IOException {
                    Files.write(dest.toPath(), content);
                }
            };
        } catch (IOException ex) {
            throw new RuntimeException("Error al cargar el archivo " + fileName + " como MultipartFile.", ex);
        }
    }

    /**
     * Obtiene la ruta del directorio temporal.
     *
     * @return ruta absoluta del directorio temporal
     */
    public String getTemporalDir() {
        return this.temporalStorageLocation.toString();
    }
}