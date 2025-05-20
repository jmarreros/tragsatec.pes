package tragsatec.pes.service.medicion;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tragsatec.pes.dto.medicion.ArchivoMedicionDTO;
import tragsatec.pes.persistence.entity.medicion.ArchivoMedicionEntity;
import tragsatec.pes.persistence.entity.medicion.MedicionEntity;
import tragsatec.pes.persistence.repository.medicion.ArchivoMedicionRepository;
import tragsatec.pes.persistence.repository.medicion.MedicionRepository; // Necesario para buscar MedicionEntity por ID

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ArchivoMedicionService {

    private final ArchivoMedicionRepository archivoMedicionRepository;
    private final MedicionRepository medicionRepository;

    private ArchivoMedicionDTO mapToDTO(ArchivoMedicionEntity entity) {
        if (entity == null) {
            return null;
        }
        ArchivoMedicionDTO dto = new ArchivoMedicionDTO();
        dto.setId(entity.getId());
        dto.setFileName(entity.getFileName());
        dto.setFilePath(entity.getFilePath());
        if (entity.getMedicion() != null) {
            dto.setMedicionId(entity.getMedicion().getId());
        }
        return dto;
    }

    private ArchivoMedicionEntity mapToEntity(ArchivoMedicionDTO dto) {
        ArchivoMedicionEntity entity = new ArchivoMedicionEntity();

        entity.setFileName(dto.getFileName());
        entity.setFilePath(dto.getFilePath());
        if (dto.getMedicionId() != null) {
            MedicionEntity medicion = medicionRepository.findById(dto.getMedicionId())
                .orElseThrow(() -> new IllegalArgumentException("Medicion no encontrada con ID: " + dto.getMedicionId()));
            entity.setMedicion(medicion);
        }
        return entity;
    }

    @Transactional(readOnly = true)
    public List<ArchivoMedicionDTO> findAll() {
        return archivoMedicionRepository.findAll().stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<ArchivoMedicionDTO> findById(Integer id) {
        return archivoMedicionRepository.findById(id).map(this::mapToDTO);
    }

    @Transactional
    public ArchivoMedicionDTO save(ArchivoMedicionDTO dto) {
        ArchivoMedicionEntity entity = mapToEntity(dto);
        ArchivoMedicionEntity savedEntity = archivoMedicionRepository.save(entity);
        return mapToDTO(savedEntity);
    }

    @Transactional
    public Optional<ArchivoMedicionDTO> update(Integer id, ArchivoMedicionDTO dto) {
        return archivoMedicionRepository.findById(id)
            .map(existingEntity -> {
                existingEntity.setFileName(dto.getFileName());
                existingEntity.setFilePath(dto.getFilePath());
                if (dto.getMedicionId() != null) {
                    MedicionEntity medicion = medicionRepository.findById(dto.getMedicionId())
                        .orElseThrow(() -> new IllegalArgumentException("Medicion no encontrada con ID: " + dto.getMedicionId()));
                    existingEntity.setMedicion(medicion);
                }
                ArchivoMedicionEntity updatedEntity = archivoMedicionRepository.save(existingEntity);
                return mapToDTO(updatedEntity);
            });
    }

}

