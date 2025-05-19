package tragsatec.pes.service.estructura;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tragsatec.pes.dto.estructura.PesUtEstacionRequestDTO;
import tragsatec.pes.dto.estructura.PesUtEstacionResponseDTO;
import tragsatec.pes.persistence.entity.estructura.PesEntity;
import tragsatec.pes.persistence.entity.estructura.PesUtEstacionEntity;
import tragsatec.pes.persistence.repository.estructura.PesUtEstacionRepository; // Asumiendo que existe este repositorio

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PesUtEstacionService {

    private final PesUtEstacionRepository pesUtEstacionRepository;
    private final PesService pesService;

    // Método helper para mapear Entity a ResponseDTO
    private PesUtEstacionResponseDTO mapToPesUtEstacionResponseDTO(PesUtEstacionEntity entity) {
        if (entity == null) {
            return null;
        }
        PesUtEstacionResponseDTO dto = new PesUtEstacionResponseDTO();
        dto.setId(entity.getId());
        dto.setUnidadTerritorialId(entity.getUnidadTerritorialId());
        dto.setEstacionId(entity.getEstacionId());
        if (entity.getPes() != null) {
            dto.setPesId(entity.getPes().getId());
        }
        dto.setCoeficiente(entity.getCoeficiente());
        return dto;
    }

    @Transactional(readOnly = true)
    public List<PesUtEstacionResponseDTO> findAll() {
        return pesUtEstacionRepository.findAll()
                .stream()
                .map(this::mapToPesUtEstacionResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<PesUtEstacionResponseDTO> findById(Integer id) {
        return pesUtEstacionRepository.findById(id)
                .map(this::mapToPesUtEstacionResponseDTO);
    }

    @Transactional
    public PesUtEstacionResponseDTO save(PesUtEstacionRequestDTO dto) {
        PesEntity pes = pesService.findById(dto.getPesId())
                .orElseThrow(() -> new IllegalArgumentException("Pes no encontrado con ID: " + dto.getPesId()));

        PesUtEstacionEntity entity = new PesUtEstacionEntity();
        entity.setUnidadTerritorialId(dto.getUnidadTerritorialId());
        entity.setEstacionId(dto.getEstacionId());
        entity.setPes(pes);
        entity.setCoeficiente(dto.getCoeficiente());

        PesUtEstacionEntity savedEntity = pesUtEstacionRepository.save(entity);
        return mapToPesUtEstacionResponseDTO(savedEntity);
    }

    @Transactional
    public Optional<PesUtEstacionResponseDTO> update(Integer id, PesUtEstacionRequestDTO dto) {
        return pesUtEstacionRepository.findById(id)
                .map(existingEntity -> {
                    if (dto.getPesId() != null) {
                        PesEntity pes = pesService.findById(dto.getPesId())
                                .orElseThrow(() -> new IllegalArgumentException("Pes no encontrado con ID: " + dto.getPesId()));
                        existingEntity.setPes(pes);
                    }
                    if (dto.getUnidadTerritorialId() != null) {
                        existingEntity.setUnidadTerritorialId(dto.getUnidadTerritorialId());
                    }
                    if (dto.getEstacionId() != null) {
                        existingEntity.setEstacionId(dto.getEstacionId());
                    }
                    if (dto.getCoeficiente() != null) {
                        existingEntity.setCoeficiente(dto.getCoeficiente());
                    }
                    PesUtEstacionEntity updatedEntity = pesUtEstacionRepository.save(existingEntity);
                    return mapToPesUtEstacionResponseDTO(updatedEntity);
                });
    }
}