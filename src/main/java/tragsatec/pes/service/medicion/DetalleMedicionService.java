package tragsatec.pes.service.medicion;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tragsatec.pes.dto.medicion.DetalleMedicionDTO;
import tragsatec.pes.persistence.entity.general.EstacionEntity;
import tragsatec.pes.persistence.entity.medicion.DetalleMedicionEntity;
import tragsatec.pes.persistence.entity.medicion.MedicionEntity;
import tragsatec.pes.persistence.repository.general.EstacionRepository;
import tragsatec.pes.persistence.repository.medicion.DetalleMedicionRepository;
import tragsatec.pes.persistence.repository.medicion.MedicionRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DetalleMedicionService {

    private final DetalleMedicionRepository detalleMedicionRepository;
    private final MedicionRepository medicionRepository;
    private final EstacionRepository estacionRepository;

    public DetalleMedicionDTO mapToDTO(DetalleMedicionEntity entity) {
        if (entity == null) {
            return null;
        }
        DetalleMedicionDTO dto = new DetalleMedicionDTO();
        dto.setId(entity.getId());
        dto.setValor(entity.getValor());
        dto.setTipoDato(entity.getTipoDato());
        if (entity.getMedicion() != null) {
            dto.setMedicionId(entity.getMedicion().getId());
        }
        if (entity.getEstacion() != null) {
            dto.setEstacionId(entity.getEstacion().getId());
        }
        return dto;
    }

    private DetalleMedicionEntity mapToEntity(DetalleMedicionDTO dto) {
        DetalleMedicionEntity entity = new DetalleMedicionEntity();
        entity.setValor(dto.getValor());
        entity.setTipoDato(dto.getTipoDato());

        if (dto.getMedicionId() != null) {
            MedicionEntity medicion = medicionRepository.findById(dto.getMedicionId())
                .orElseThrow(() -> new IllegalArgumentException("Medicion no encontrada con ID: " + dto.getMedicionId()));
            entity.setMedicion(medicion);
        }
        if (dto.getEstacionId() != null) {
            EstacionEntity estacion = estacionRepository.findById(dto.getEstacionId())
                .orElseThrow(() -> new IllegalArgumentException("Estacion no encontrada con ID: " + dto.getEstacionId()));
            entity.setEstacion(estacion);
        }
        return entity;
    }

    @Transactional(readOnly = true)
    public List<DetalleMedicionDTO> findAll() {
        return detalleMedicionRepository.findAll().stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<DetalleMedicionDTO> findById(Long id) {
        return detalleMedicionRepository.findById(id).map(this::mapToDTO);
    }

    @Transactional
    public DetalleMedicionDTO save(DetalleMedicionDTO dto) {
        DetalleMedicionEntity entity = mapToEntity(dto);
        DetalleMedicionEntity savedEntity = detalleMedicionRepository.save(entity);
        return mapToDTO(savedEntity);
    }

    @Transactional
    public Optional<DetalleMedicionDTO> update(Long id, DetalleMedicionDTO dto) {
        return detalleMedicionRepository.findById(id)
            .map(existingEntity -> {
                existingEntity.setValor(dto.getValor());
                existingEntity.setTipoDato(dto.getTipoDato());
                if (dto.getMedicionId() != null) {
                    MedicionEntity medicion = medicionRepository.findById(dto.getMedicionId())
                        .orElseThrow(() -> new IllegalArgumentException("Medicion no encontrada con ID: " + dto.getMedicionId()));
                    existingEntity.setMedicion(medicion);
                }
                if (dto.getEstacionId() != null) {
                    EstacionEntity estacion = estacionRepository.findById(dto.getEstacionId())
                        .orElseThrow(() -> new IllegalArgumentException("Estacion no encontrada con ID: " + dto.getEstacionId()));
                    existingEntity.setEstacion(estacion);
                }
                DetalleMedicionEntity updatedEntity = detalleMedicionRepository.save(existingEntity);
                return mapToDTO(updatedEntity);
            });
    }
}