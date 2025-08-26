package com.chc.pes.service.estructura;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.chc.pes.dto.estructura.PesDemarcacionUtRequestDTO;
import com.chc.pes.dto.estructura.PesDemarcacionUtResponseDTO;
import com.chc.pes.persistence.entity.estructura.PesDemarcacionUtEntity;
import com.chc.pes.persistence.entity.estructura.PesEntity;
import com.chc.pes.persistence.entity.general.DemarcacionEntity;
import com.chc.pes.persistence.entity.general.UnidadTerritorialEntity;
import com.chc.pes.persistence.repository.estructura.PesDemarcacionUtRepository;
import com.chc.pes.service.general.UnidadTerritorialService;
import com.chc.pes.service.general.DemarcacionService;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PesDemarcacionUtService {

    private final PesDemarcacionUtRepository pesDemarcacionUtRepository;
    private final PesService pesService;
    private final UnidadTerritorialService unidadTerritorialService;
    private final DemarcacionService demarcacionService;

    // Método helper para mapear Entity a ResponseDTO
    private PesDemarcacionUtResponseDTO mapToPesDemarcacionUtResponseDTO(PesDemarcacionUtEntity entity) {
        if (entity == null) {
            return null;
        }
        PesDemarcacionUtResponseDTO dto = new PesDemarcacionUtResponseDTO();
        dto.setId(entity.getId());
        if (entity.getUnidadTerritorial() != null) {
            dto.setUnidadTerritorialId(entity.getUnidadTerritorial().getId());
        }
        if (entity.getDemarcacion() != null) {
            dto.setDemarcacionId(entity.getDemarcacion().getId());
        }
        if (entity.getPes() != null) {
            dto.setPesId(entity.getPes().getId());
        }
        dto.setTipo(entity.getTipo());
        dto.setCoeficiente(entity.getCoeficiente());
        return dto;
    }

    // Método helper para mapear RequestDTO y entidades a Entity (para creación)
    private PesDemarcacionUtEntity mapToPesDemarcacionUtEntity(PesDemarcacionUtRequestDTO dto) {
        PesDemarcacionUtEntity entity = new PesDemarcacionUtEntity();

        UnidadTerritorialEntity ut = unidadTerritorialService.findById(dto.getUnidadTerritorialId())
                .orElseThrow(()  -> new IllegalArgumentException("UnidadTerritorial no encontrada con ID: " + dto.getUnidadTerritorialId())); // Mensaje de error corregido
        entity.setUnidadTerritorial(ut);

        DemarcacionEntity demarcacion = demarcacionService.findById(dto.getDemarcacionId())
                .orElseThrow(() -> new IllegalArgumentException("Demarcación no encontrada con ID: " + dto.getDemarcacionId()));
        entity.setDemarcacion(demarcacion);

        PesEntity pes = pesService.findById(dto.getPesId())
                .orElseThrow(() -> new IllegalArgumentException("Pes no encontrado con ID: " + dto.getPesId()));
        entity.setPes(pes);

        entity.setTipo(dto.getTipo());
        entity.setCoeficiente(dto.getCoeficiente());
        return entity;
    }

    @Transactional(readOnly = true)
    public List<PesDemarcacionUtResponseDTO> findAll() {
        return pesDemarcacionUtRepository.findAll()
                .stream()
                .map(this::mapToPesDemarcacionUtResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<PesDemarcacionUtResponseDTO> findByIdDto(Integer id) {
        return pesDemarcacionUtRepository.findById(id)
                .map(this::mapToPesDemarcacionUtResponseDTO);
    }

    @Transactional
    public PesDemarcacionUtResponseDTO save(PesDemarcacionUtRequestDTO dto) {
        PesDemarcacionUtEntity entity = mapToPesDemarcacionUtEntity(dto);
        // Validar tipo
        if (dto.getTipo() == null || (dto.getTipo() != 'E' && dto.getTipo() != 'S')) {
            throw new IllegalArgumentException("El campo 'tipo' es obligatorio y debe ser 'E' (Escasez) o 'S' (Sequía).");
        }
        entity.setTipo(dto.getTipo());
        PesDemarcacionUtEntity savedEntity = pesDemarcacionUtRepository.save(entity);
        return mapToPesDemarcacionUtResponseDTO(savedEntity);
    }

    @Transactional
    public Optional<PesDemarcacionUtResponseDTO> update(Integer id, PesDemarcacionUtRequestDTO dto) {
        return pesDemarcacionUtRepository.findById(id)
                .map(existingEntity -> {
                    if (dto.getCoeficiente() != null) {
                        existingEntity.setCoeficiente(dto.getCoeficiente());
                    }
                    if (dto.getTipo() != null) {
                        if (dto.getTipo() != 'E' && dto.getTipo() != 'S') {
                            throw new IllegalArgumentException("El campo 'tipo' debe ser 'E' (Escasez) o 'S' (Sequía).");
                        }
                        existingEntity.setTipo(dto.getTipo());
                    }

                    if (dto.getUnidadTerritorialId() != null) {
                        UnidadTerritorialEntity ut = unidadTerritorialService.findById(dto.getUnidadTerritorialId())
                                .orElseThrow(() -> new IllegalArgumentException("UnidadTerritorial no encontrada con ID: " + dto.getUnidadTerritorialId()));
                        existingEntity.setUnidadTerritorial(ut);
                    }
                    if (dto.getDemarcacionId() != null) {
                        DemarcacionEntity demarcacion = demarcacionService.findById(dto.getDemarcacionId())
                                .orElseThrow(() -> new IllegalArgumentException("Demarcación no encontrada con ID: " + dto.getDemarcacionId()));
                        existingEntity.setDemarcacion(demarcacion);
                    }
                    if (dto.getPesId() != null) {
                        PesEntity pes = pesService.findById(dto.getPesId())
                                .orElseThrow(() -> new IllegalArgumentException("Pes no encontrado con ID: " + dto.getPesId()));
                        existingEntity.setPes(pes);
                    }

                    PesDemarcacionUtEntity updatedEntity = pesDemarcacionUtRepository.save(existingEntity);
                    return mapToPesDemarcacionUtResponseDTO(updatedEntity);
                });
    }
}
