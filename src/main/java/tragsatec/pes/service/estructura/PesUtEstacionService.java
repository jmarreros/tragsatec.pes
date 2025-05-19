package tragsatec.pes.service.estructura; // O el paquete donde residiría este servicio

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tragsatec.pes.dto.estructura.PesUtEstacionRequestDTO;
import tragsatec.pes.dto.estructura.PesUtEstacionResponseDTO;
import tragsatec.pes.persistence.entity.estructura.PesEntity;
import tragsatec.pes.persistence.entity.estructura.PesUtEstacionEntity;
import tragsatec.pes.persistence.entity.general.EstacionEntity;
import tragsatec.pes.persistence.entity.general.UnidadTerritorialEntity;
import tragsatec.pes.persistence.repository.estructura.PesUtEstacionRepository; // Asumiendo este repositorio
import tragsatec.pes.service.general.EstacionService;
import tragsatec.pes.service.general.UnidadTerritorialService;

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
}