package tragsatec.pes.service.estructura;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tragsatec.pes.dto.estructura.PesDemarcacionUtRequestDTO;
import tragsatec.pes.dto.estructura.PesDemarcacionUtResponseDTO;
import tragsatec.pes.persistence.entity.estructura.PesDemarcacionUtEntity;
import tragsatec.pes.persistence.entity.estructura.PesEntity;
import tragsatec.pes.persistence.repository.estructura.PesDemarcacionUtRepository; // Asume que este repositorio existe

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PesDemarcacionUtService {

    private final PesDemarcacionUtRepository pesDemarcacionUtRepository;
    private final PesService pesService;

    // Método helper para mapear Entity a ResponseDTO
    private PesDemarcacionUtResponseDTO mapToPesDemarcacionUtResponseDTO(PesDemarcacionUtEntity entity) {
        if (entity == null) {
            return null;
        }
        PesDemarcacionUtResponseDTO dto = new PesDemarcacionUtResponseDTO();
        dto.setId(entity.getId());
        dto.setUnidadTerritorialId(entity.getUnidadTerritorialId());
        dto.setDemarcacionId(entity.getDemarcacionId());
        if (entity.getPes() != null) {
            dto.setPesId(entity.getPes().getId());
        }
        dto.setCoeficiente(entity.getCoeficiente());
        return dto;
    }

    @Transactional(readOnly = true)
    public List<PesDemarcacionUtResponseDTO> findAll() {
        return pesDemarcacionUtRepository.findAll()
                .stream()
                .map(this::mapToPesDemarcacionUtResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<PesDemarcacionUtResponseDTO> findById(Integer id) {
        return pesDemarcacionUtRepository.findById(id)
                .map(this::mapToPesDemarcacionUtResponseDTO);
    }

    @Transactional
    public PesDemarcacionUtResponseDTO save(PesDemarcacionUtRequestDTO dto) {
        PesEntity pes = pesService.findById(dto.getPesId())
                .orElseThrow(() -> new IllegalArgumentException("Pes no encontrado con ID: " + dto.getPesId()));

        PesDemarcacionUtEntity entity = new PesDemarcacionUtEntity();
        entity.setUnidadTerritorialId(dto.getUnidadTerritorialId());
        entity.setDemarcacionId(dto.getDemarcacionId());
        entity.setPes(pes);
        entity.setCoeficiente(dto.getCoeficiente());

        PesDemarcacionUtEntity savedEntity = pesDemarcacionUtRepository.save(entity);
        return mapToPesDemarcacionUtResponseDTO(savedEntity);
    }

    @Transactional
    public Optional<PesDemarcacionUtResponseDTO> update(Integer id, PesDemarcacionUtRequestDTO dto) { // Acepta PesDemarcacionUtDTO
        return pesDemarcacionUtRepository.findById(id)
                .map(existingEntity -> {
                    if (dto.getPesId() != null) {
                        PesEntity pes = pesService.findById(dto.getPesId())
                                .orElseThrow(() -> new IllegalArgumentException("Pes no encontrado con ID: " + dto.getPesId()));
                        existingEntity.setPes(pes);
                    }
                    if (dto.getUnidadTerritorialId() != null) {
                        existingEntity.setUnidadTerritorialId(dto.getUnidadTerritorialId());
                    }
                    if (dto.getDemarcacionId() != null) {
                        existingEntity.setDemarcacionId(dto.getDemarcacionId());
                    }
                    if (dto.getCoeficiente() != null) {
                        existingEntity.setCoeficiente(dto.getCoeficiente());
                    }
                    PesDemarcacionUtEntity updatedEntity = pesDemarcacionUtRepository.save(existingEntity);
                    return mapToPesDemarcacionUtResponseDTO(updatedEntity);
                });
    }
}
