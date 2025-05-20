package tragsatec.pes.service.estructura;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tragsatec.pes.dto.estructura.PesUmbralEscasezDTO;
import tragsatec.pes.persistence.entity.estructura.PesEntity;
import tragsatec.pes.persistence.entity.estructura.PesUmbralEscasezEntity;
import tragsatec.pes.persistence.entity.general.EstacionEntity;
import tragsatec.pes.persistence.entity.general.UnidadTerritorialEntity;
import tragsatec.pes.persistence.repository.estructura.PesUmbralEscasezRepository;
import tragsatec.pes.service.general.EstacionService;
import tragsatec.pes.service.general.UnidadTerritorialService;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PesUmbralEscasezService {

    private final PesUmbralEscasezRepository pesUmbralEscasezRepository;
    private final PesService pesService;
    private final EstacionService estacionService;
    private final UnidadTerritorialService unidadTerritorialService;

    private PesUmbralEscasezDTO mapToPesUmbralEscasezDTO(PesUmbralEscasezEntity entity) {
        if (entity == null) {
            return null;
        }
        PesUmbralEscasezDTO dto = new PesUmbralEscasezDTO();
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
        dto.setEscenario(entity.getEscenario());
        dto.setEstadistico(entity.getEstadistico());
        dto.setMes10(entity.getMes10());
        dto.setMes11(entity.getMes11());
        dto.setMes12(entity.getMes12());
        dto.setMes1(entity.getMes1());
        dto.setMes2(entity.getMes2());
        dto.setMes3(entity.getMes3());
        dto.setMes4(entity.getMes4());
        dto.setMes5(entity.getMes5());
        dto.setMes6(entity.getMes6());
        dto.setMes7(entity.getMes7());
        dto.setMes8(entity.getMes8());
        dto.setMes9(entity.getMes9());
        return dto;
    }

    private PesUmbralEscasezEntity mapToPesUmbralEscasezEntity(PesUmbralEscasezDTO dto) {
        PesUmbralEscasezEntity entity = new PesUmbralEscasezEntity();
        if (dto.getPesId() != null) {
            PesEntity pes = pesService.findById(dto.getPesId())
                    .orElseThrow(() -> new IllegalArgumentException("Pes no encontrado con ID: " + dto.getPesId()));
            entity.setPes(pes);
        }

        // Buscar y asignar EstacionEntity
        if (dto.getEstacionId() != null) {
            EstacionEntity estacion = estacionService.findById(dto.getEstacionId())
                    .orElseThrow(() -> new IllegalArgumentException("Estacion no encontrada con ID: " + dto.getEstacionId()));
            entity.setEstacion(estacion); // Usar setEstacion
        }

        // Buscar y asignar UnidadTerritorialEntity
        if (dto.getUnidadTerritorialId() != null) {
            UnidadTerritorialEntity unidadTerritorial = unidadTerritorialService.findById(dto.getUnidadTerritorialId())
                    .orElseThrow(() -> new IllegalArgumentException("UnidadTerritorial no encontrada con ID: " + dto.getUnidadTerritorialId()));
            entity.setUnidadTerritorial(unidadTerritorial); // Usar setUnidadTerritorial
        }

        entity.setEscenario(dto.getEscenario());
        entity.setEstadistico(dto.getEstadistico());
        entity.setMes10(dto.getMes10());
        entity.setMes11(dto.getMes11());
        entity.setMes12(dto.getMes12());
        entity.setMes1(dto.getMes1());
        entity.setMes2(dto.getMes2());
        entity.setMes3(dto.getMes3());
        entity.setMes4(dto.getMes4());
        entity.setMes5(dto.getMes5());
        entity.setMes6(dto.getMes6());
        entity.setMes7(dto.getMes7());
        entity.setMes8(dto.getMes8());
        entity.setMes9(dto.getMes9());
        return entity;
    }

    @Transactional(readOnly = true)
    public List<PesUmbralEscasezDTO> findAll() {
        return pesUmbralEscasezRepository.findAll()
                .stream()
                .map(this::mapToPesUmbralEscasezDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<PesUmbralEscasezDTO> findByIdDto(Integer id) {
        return pesUmbralEscasezRepository.findById(id)
                .map(this::mapToPesUmbralEscasezDTO);
    }

    @Transactional
    public PesUmbralEscasezDTO save(PesUmbralEscasezDTO dto) {
        PesUmbralEscasezEntity entity = mapToPesUmbralEscasezEntity(dto);
        PesUmbralEscasezEntity savedEntity = pesUmbralEscasezRepository.save(entity);
        return mapToPesUmbralEscasezDTO(savedEntity);
    }

    @Transactional
    public Optional<PesUmbralEscasezDTO> update(Integer id, PesUmbralEscasezDTO dto) {
        return pesUmbralEscasezRepository.findById(id)
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
                        existingEntity.setEstacion(estacion); // Usar setEstacion
                    } else {
                        existingEntity.setEstacion(null); // Opcional: manejar desasociación
                    }

                    // Actualizar UnidadTerritorialEntity
                    if (dto.getUnidadTerritorialId() != null) {
                        UnidadTerritorialEntity unidadTerritorial = unidadTerritorialService.findById(dto.getUnidadTerritorialId())
                                .orElseThrow(() -> new IllegalArgumentException("UnidadTerritorial no encontrada con ID: " + dto.getUnidadTerritorialId()));
                        existingEntity.setUnidadTerritorial(unidadTerritorial); // Usar setUnidadTerritorial
                    } else {
                        existingEntity.setUnidadTerritorial(null);
                    }

                    existingEntity.setEscenario(dto.getEscenario());
                    existingEntity.setEstadistico(dto.getEstadistico());
                    existingEntity.setMes10(dto.getMes10());
                    existingEntity.setMes11(dto.getMes11());
                    existingEntity.setMes12(dto.getMes12());
                    existingEntity.setMes1(dto.getMes1());
                    existingEntity.setMes2(dto.getMes2());
                    existingEntity.setMes3(dto.getMes3());
                    existingEntity.setMes4(dto.getMes4());
                    existingEntity.setMes5(dto.getMes5());
                    existingEntity.setMes6(dto.getMes6());
                    existingEntity.setMes7(dto.getMes7());
                    existingEntity.setMes8(dto.getMes8());
                    existingEntity.setMes9(dto.getMes9());
                    PesUmbralEscasezEntity updatedEntity = pesUmbralEscasezRepository.save(existingEntity);
                    return mapToPesUmbralEscasezDTO(updatedEntity);
                });
    }
}