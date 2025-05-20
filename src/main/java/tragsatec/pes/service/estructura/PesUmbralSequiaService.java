package tragsatec.pes.service.estructura;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tragsatec.pes.dto.estructura.PesUmbralSequiaDTO;
import tragsatec.pes.persistence.entity.estructura.PesEntity;
import tragsatec.pes.persistence.entity.estructura.PesUmbralSequiaEntity;
import tragsatec.pes.persistence.entity.general.EstacionEntity;
import tragsatec.pes.persistence.entity.general.UnidadTerritorialEntity;
import tragsatec.pes.persistence.repository.estructura.PesUmbralSequiaRepository;
import tragsatec.pes.service.general.EstacionService;
import tragsatec.pes.service.general.UnidadTerritorialService;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PesUmbralSequiaService {

    private final PesUmbralSequiaRepository pesUmbralSequiaRepository;
    private final PesService pesService;
    private final EstacionService estacionService; // Inyectar EstacionService
    private final UnidadTerritorialService unidadTerritorialService; // Inyectar UnidadTerritorialService

    private PesUmbralSequiaDTO mapToPesUmbralSequiaDTO(PesUmbralSequiaEntity entity) {
        if (entity == null) {
            return null;
        }
        PesUmbralSequiaDTO dto = new PesUmbralSequiaDTO();
        dto.setId(entity.getId());
        if (entity.getPes() != null) {
            dto.setPesId(entity.getPes().getId());
        }

        if (entity.getEstacion() != null) {
            dto.setEstacionId(entity.getEstacion().getId());
        }
        if (entity.getUnidadTerritorial() != null) {
            dto.setUnidadTerritorialId(entity.getUnidadTerritorial().getId());
        }
        dto.setMes(entity.getMes());
        dto.setPromedioPrep1(entity.getPromedioPrep1());
        dto.setPromedioPrep3(entity.getPromedioPrep3());
        dto.setPromedioPrep6(entity.getPromedioPrep6());
        dto.setMaxPrep1(entity.getMaxPrep1());
        dto.setMaxPrep3(entity.getMaxPrep3());
        dto.setMaxPrep6(entity.getMaxPrep6());
        dto.setMinPrep1(entity.getMinPrep1());
        dto.setMinPrep3(entity.getMinPrep3());
        dto.setMinPrep6(entity.getMinPrep6());
        dto.setDesvPrep1(entity.getDesvPrep1());
        dto.setDesvPrep3(entity.getDesvPrep3());
        dto.setDesvPrep6(entity.getDesvPrep6());
        return dto;
    }

    private PesUmbralSequiaEntity mapToPesUmbralSequiaEntity(PesUmbralSequiaDTO dto) {
        PesUmbralSequiaEntity entity = new PesUmbralSequiaEntity();
        if (dto.getPesId() != null) {
            PesEntity pes = pesService.findById(dto.getPesId())
                .orElseThrow(() -> new IllegalArgumentException("Pes no encontrado con ID: " + dto.getPesId()));
            entity.setPes(pes);
        }

        // Buscar y asignar EstacionEntity
        if (dto.getEstacionId() != null) {
            EstacionEntity estacion = estacionService.findById(dto.getEstacionId())
                    .orElseThrow(() -> new IllegalArgumentException("Estacion no encontrada con ID: " + dto.getEstacionId()));
            entity.setEstacion(estacion);
        }

        // Buscar y asignar UnidadTerritorialEntity
        if (dto.getUnidadTerritorialId() != null) {
            UnidadTerritorialEntity unidadTerritorial = unidadTerritorialService.findById(dto.getUnidadTerritorialId())
                    .orElseThrow(() -> new IllegalArgumentException("UnidadTerritorial no encontrada con ID: " + dto.getUnidadTerritorialId()));
            entity.setUnidadTerritorial(unidadTerritorial);
        }

        entity.setMes(dto.getMes());
        entity.setPromedioPrep1(dto.getPromedioPrep1());
        entity.setPromedioPrep3(dto.getPromedioPrep3());
        entity.setPromedioPrep6(dto.getPromedioPrep6());
        entity.setMaxPrep1(dto.getMaxPrep1());
        entity.setMaxPrep3(dto.getMaxPrep3());
        entity.setMaxPrep6(dto.getMaxPrep6());
        entity.setMinPrep1(dto.getMinPrep1());
        entity.setMinPrep3(dto.getMinPrep3());
        entity.setMinPrep6(dto.getMinPrep6());
        entity.setDesvPrep1(dto.getDesvPrep1());
        entity.setDesvPrep3(dto.getDesvPrep3());
        entity.setDesvPrep6(dto.getDesvPrep6());
        return entity;
    }

    @Transactional(readOnly = true)
    public List<PesUmbralSequiaDTO> findAll() {
        return pesUmbralSequiaRepository.findAll()
                .stream()
                .map(this::mapToPesUmbralSequiaDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<PesUmbralSequiaDTO> findByIdDto(Integer id) {
        return pesUmbralSequiaRepository.findById(id)
                .map(this::mapToPesUmbralSequiaDTO);
    }

    @Transactional
    public PesUmbralSequiaDTO save(PesUmbralSequiaDTO dto) {
        PesUmbralSequiaEntity entity = mapToPesUmbralSequiaEntity(dto);
        PesUmbralSequiaEntity savedEntity = pesUmbralSequiaRepository.save(entity);
        return mapToPesUmbralSequiaDTO(savedEntity);
    }

    @Transactional
    public Optional<PesUmbralSequiaDTO> update(Integer id, PesUmbralSequiaDTO dto) {
        return pesUmbralSequiaRepository.findById(id)
                .map(existingEntity -> {
                    if (dto.getPesId() != null) {
                        PesEntity pes = pesService.findById(dto.getPesId())
                            .orElseThrow(() -> new IllegalArgumentException("Pes no encontrado con ID: " + dto.getPesId()));
                        existingEntity.setPes(pes);
                    }

                    // Actualizar EstacionEntity
                    if (dto.getEstacionId() != null) {
                        EstacionEntity estacion = estacionService.findById(dto.getEstacionId())
                                .orElseThrow(() -> new IllegalArgumentException("Estacion no encontrada con ID: " + dto.getEstacionId()));
                        existingEntity.setEstacion(estacion);
                    }

                    // Actualizar UnidadTerritorialEntity
                    if (dto.getUnidadTerritorialId() != null) {
                        UnidadTerritorialEntity unidadTerritorial = unidadTerritorialService.findById(dto.getUnidadTerritorialId())
                                .orElseThrow(() -> new IllegalArgumentException("UnidadTerritorial no encontrada con ID: " + dto.getUnidadTerritorialId()));
                        existingEntity.setUnidadTerritorial(unidadTerritorial);
                    }

                    existingEntity.setMes(dto.getMes());
                    existingEntity.setPromedioPrep1(dto.getPromedioPrep1());
                    existingEntity.setPromedioPrep3(dto.getPromedioPrep3());
                    existingEntity.setPromedioPrep6(dto.getPromedioPrep6());
                    existingEntity.setMaxPrep1(dto.getMaxPrep1());
                    existingEntity.setMaxPrep3(dto.getMaxPrep3());
                    existingEntity.setMaxPrep6(dto.getMaxPrep6());
                    existingEntity.setMinPrep1(dto.getMinPrep1());
                    existingEntity.setMinPrep3(dto.getMinPrep3());
                    existingEntity.setMinPrep6(dto.getMinPrep6());
                    existingEntity.setDesvPrep1(dto.getDesvPrep1());
                    existingEntity.setDesvPrep3(dto.getDesvPrep3());
                    existingEntity.setDesvPrep6(dto.getDesvPrep6());
                    PesUmbralSequiaEntity updatedEntity = pesUmbralSequiaRepository.save(existingEntity);
                    return mapToPesUmbralSequiaDTO(updatedEntity);
                });
    }

}