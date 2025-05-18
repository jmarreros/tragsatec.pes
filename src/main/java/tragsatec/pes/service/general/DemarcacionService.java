package tragsatec.pes.service.general;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tragsatec.pes.dto.DemarcacionResponseDTO;
import tragsatec.pes.dto.DemarcacionSummaryDTO;
import tragsatec.pes.dto.UnidadTerritorialSummaryDTO;
import tragsatec.pes.persistence.entity.general.DemarcacionEntity;
import tragsatec.pes.persistence.entity.general.UnidadTerritorialEntity;
import tragsatec.pes.persistence.repository.general.DemarcacionRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class DemarcacionService {
    private final DemarcacionRepository demarcacionRepository;

    @Autowired
    public DemarcacionService(DemarcacionRepository demarcacionRepository) {
        this.demarcacionRepository = demarcacionRepository;
    }

    private DemarcacionSummaryDTO mapToDemarcacionSummaryDTO(DemarcacionEntity entity) {
        if (entity == null) {
            return null;
        }
        DemarcacionSummaryDTO dto = new DemarcacionSummaryDTO();
        dto.setId(entity.getId());
        dto.setCodigo(entity.getCodigo());
        dto.setNombre(entity.getNombre());
        return dto;
    }

    private UnidadTerritorialSummaryDTO mapToUnidadTerritorialSummaryDTO(UnidadTerritorialEntity utEntity) {
        if (utEntity == null) {
            return null;
        }
        UnidadTerritorialSummaryDTO utDto = new UnidadTerritorialSummaryDTO();
        utDto.setId(utEntity.getId());
        utDto.setCodigo(utEntity.getCodigo());
        utDto.setNombre(utEntity.getNombre());
        return utDto;
    }

    private DemarcacionResponseDTO mapToDemarcacionResponseDTO(DemarcacionEntity entity) {
        if (entity == null) {
            return null;
        }
        DemarcacionResponseDTO dto = new DemarcacionResponseDTO();
        dto.setId(entity.getId());
        dto.setCodigo(entity.getCodigo());
        dto.setNombre(entity.getNombre());

        if (entity.getUnidadesTerritoriales() != null && !entity.getUnidadesTerritoriales().isEmpty()) {
            List<UnidadTerritorialSummaryDTO> utDtos = entity.getUnidadesTerritoriales().stream()
                    .map(this::mapToUnidadTerritorialSummaryDTO)
                    .collect(Collectors.toList());
            dto.setUnidadesTerritoriales(utDtos);
        } else {
            dto.setUnidadesTerritoriales(new ArrayList<>());
        }
        return dto;
    }

    @Transactional(readOnly = true)
    public List<DemarcacionSummaryDTO> findAll() {
        Iterable<DemarcacionEntity> entities = demarcacionRepository.findAll();
        return StreamSupport.stream(entities.spliterator(), false)
                .map(this::mapToDemarcacionSummaryDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<DemarcacionEntity> findById(Integer id) {
        return demarcacionRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<DemarcacionSummaryDTO> findByIdAsSummaryDTO(Integer id) {
        return demarcacionRepository.findById(id)
                .map(this::mapToDemarcacionSummaryDTO);
    }

    @Transactional(readOnly = true)
    public Optional<DemarcacionResponseDTO> findByIdWithUnidadesTerritoriales(Integer id) {
        return demarcacionRepository.findById(id)
                .map(this::mapToDemarcacionResponseDTO);
    }

    @Transactional
    public DemarcacionSummaryDTO save(DemarcacionEntity entity) {
        DemarcacionEntity savedEntity = demarcacionRepository.save(entity);
        return mapToDemarcacionSummaryDTO(savedEntity);
    }
}