package com.chc.pes.service.estructura; // O el paquete donde residiría este servicio

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.chc.pes.dto.general.EstacionProjection;
import com.chc.pes.dto.estructura.PesUtEstacionRequestDTO;
import com.chc.pes.dto.estructura.PesUtEstacionResponseDTO;
import com.chc.pes.persistence.entity.estructura.PesEntity;
import com.chc.pes.persistence.entity.estructura.PesUtEstacionEntity;
import com.chc.pes.persistence.entity.general.EstacionEntity;
import com.chc.pes.persistence.entity.general.UnidadTerritorialEntity;
import com.chc.pes.persistence.repository.estructura.PesUtEstacionRepository; // Asumiendo este repositorio
import com.chc.pes.service.general.EstacionService;
import com.chc.pes.service.general.UnidadTerritorialService;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PesUtEstacionService {

    private final PesUtEstacionRepository pesUtEstacionRepository;
    private final PesService pesService;
    private final UnidadTerritorialService unidadTerritorialService;
    private final EstacionService estacionService;

    private PesUtEstacionResponseDTO mapToPesUtEstacionResponseDTO(PesUtEstacionEntity entity) {
        if (entity == null) {
            return null;
        }
        PesUtEstacionResponseDTO dto = new PesUtEstacionResponseDTO();
        dto.setId(entity.getId());
        if (entity.getUnidadTerritorial() != null) {
            dto.setUnidadTerritorialId(entity.getUnidadTerritorial().getId());
        }
        if (entity.getEstacion() != null) {
            dto.setEstacionId(entity.getEstacion().getId());
        }
        if (entity.getPes() != null) {
            dto.setPesId(entity.getPes().getId());
        }
        dto.setTipo(entity.getTipo());
        dto.setCoeficiente(entity.getCoeficiente());
        return dto;
    }

    private PesUtEstacionEntity mapToPesUtEstacionEntity(PesUtEstacionRequestDTO dto) {
        PesUtEstacionEntity entity = new PesUtEstacionEntity();

        UnidadTerritorialEntity ut = unidadTerritorialService.findById(dto.getUnidadTerritorialId())
                .orElseThrow(() -> new IllegalArgumentException("UnidadTerritorial no encontrada con ID: " + dto.getUnidadTerritorialId()));
        entity.setUnidadTerritorial(ut);

        EstacionEntity estacion = estacionService.findById(dto.getEstacionId())
                .orElseThrow(() -> new IllegalArgumentException("Estacion no encontrada con ID: " + dto.getEstacionId()));
        entity.setEstacion(estacion);

        PesEntity pes = pesService.findById(dto.getPesId())
                .orElseThrow(() -> new IllegalArgumentException("Pes no encontrado con ID: " + dto.getPesId()));
        entity.setPes(pes);

        entity.setTipo(dto.getTipo());
        entity.setCoeficiente(dto.getCoeficiente());
        return entity;
    }

    @Transactional(readOnly = true)
    public List<PesUtEstacionResponseDTO> findAll() {
        return pesUtEstacionRepository.findAll()
                .stream()
                .map(this::mapToPesUtEstacionResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<PesUtEstacionResponseDTO> findByIdDto(Integer id) {
        return pesUtEstacionRepository.findById(id)
                .map(this::mapToPesUtEstacionResponseDTO);
    }

    @Transactional
    public PesUtEstacionResponseDTO save(PesUtEstacionRequestDTO dto) {
        PesUtEstacionEntity entity = mapToPesUtEstacionEntity(dto);
        // Validar tipo
        if (dto.getTipo() == null || (dto.getTipo() != 'E' && dto.getTipo() != 'S')) {
            throw new IllegalArgumentException("El campo 'tipo' es obligatorio y debe ser 'E' (Escasez) o 'S' (Sequía).");
        }
        entity.setTipo(dto.getTipo());
        PesUtEstacionEntity savedEntity = pesUtEstacionRepository.save(entity);
        return mapToPesUtEstacionResponseDTO(savedEntity);
    }

    @Transactional
    public Optional<PesUtEstacionResponseDTO> update(Integer id, PesUtEstacionRequestDTO dto) {
        return pesUtEstacionRepository.findById(id)
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
                    if (dto.getEstacionId() != null) {
                        EstacionEntity estacion = estacionService.findById(dto.getEstacionId())
                                .orElseThrow(() -> new IllegalArgumentException("Estacion no encontrada con ID: " + dto.getEstacionId()));
                        existingEntity.setEstacion(estacion);
                    }
                    if (dto.getPesId() != null) {
                        PesEntity pes = pesService.findById(dto.getPesId())
                                .orElseThrow(() -> new IllegalArgumentException("Pes no encontrado con ID: " + dto.getPesId()));
                        existingEntity.setPes(pes);
                    }
                    PesUtEstacionEntity updatedEntity = pesUtEstacionRepository.save(existingEntity);
                    return mapToPesUtEstacionResponseDTO(updatedEntity);
                });
    }

    @Transactional(readOnly = true)
    public List<EstacionProjection> getEstacionesByPesId(Integer pesId, Character tipo) {
        return pesUtEstacionRepository.getAllEstacionesByPesId(pesId, tipo);
    }

}
