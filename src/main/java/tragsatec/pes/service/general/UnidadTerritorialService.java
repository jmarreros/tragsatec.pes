package tragsatec.pes.service.general;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tragsatec.pes.dto.general.*;
import tragsatec.pes.persistence.entity.general.DemarcacionEntity;
import tragsatec.pes.persistence.entity.general.EstacionEntity;
import tragsatec.pes.persistence.entity.general.EstacionUtEntity;
import tragsatec.pes.persistence.entity.general.UnidadTerritorialEntity;
import tragsatec.pes.persistence.repository.general.DemarcacionRepository;
import tragsatec.pes.persistence.repository.general.EstacionRepository;
import tragsatec.pes.persistence.repository.general.EstacionUtRepository;
import tragsatec.pes.persistence.repository.general.UnidadTerritorialRepository;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UnidadTerritorialService {

    private final DemarcacionRepository demarcacionRepository;
    private final EstacionRepository estacionRepository;
    private final EstacionUtRepository estacionUtRepository;
    private final UnidadTerritorialRepository unidadTerritorialRepository;

    @Autowired
    public UnidadTerritorialService(UnidadTerritorialRepository unidadTerritorialRepository,
                                    DemarcacionRepository demarcacionRepository,
                                    EstacionRepository estacionRepository,
                                    EstacionUtRepository estacionUtRepository) {
        this.unidadTerritorialRepository = unidadTerritorialRepository;
        this.demarcacionRepository = demarcacionRepository;
        this.estacionRepository = estacionRepository;
        this.estacionUtRepository = estacionUtRepository;
    }

    private UnidadTerritorialSummaryDTO mapToUnidadTerritorialSummaryDTO(UnidadTerritorialEntity entity) {
        if (entity == null) return null;
        UnidadTerritorialSummaryDTO dto = new UnidadTerritorialSummaryDTO();
        dto.setId(entity.getId());
        dto.setCodigo(entity.getCodigo());
        dto.setNombre(entity.getNombre());
        dto.setImagen(entity.getImagen());
        return dto;
    }

    private UnidadTerritorialResponseDTO mapToUnidadTerritorialResponseDTO(UnidadTerritorialEntity entity) {
        if (entity == null) return null;
        UnidadTerritorialResponseDTO dto = new UnidadTerritorialResponseDTO();
        dto.setId(entity.getId());
        dto.setCodigo(entity.getCodigo());
        dto.setNombre(entity.getNombre());
        dto.setComentario(entity.getComentario());
        dto.setImagen(entity.getImagen());

        if (entity.getDemarcacion() != null) {
            DemarcacionSummaryDTO demDto = new DemarcacionSummaryDTO();
            demDto.setId(entity.getDemarcacion().getId());
            demDto.setCodigo(entity.getDemarcacion().getCodigo());
            demDto.setNombre(entity.getDemarcacion().getNombre());
            dto.setDemarcacion(demDto);
        }

        if (entity.getEstacionesUt() != null) {
            List<EstacionSummaryDTO> estDtos = entity.getEstacionesUt().stream()
                    .map(EstacionUtEntity::getEstacion)
                    .map(estacionEntity -> {
                        EstacionSummaryDTO estDto = new EstacionSummaryDTO();
                        estDto.setId(estacionEntity.getId());
                        estDto.setCodigo(estacionEntity.getCodigo());
                        estDto.setNombre(estacionEntity.getNombre());
                        return estDto;
                    })
                    .collect(Collectors.toList());
            dto.setEstaciones(estDtos);
        }
        return dto;
    }

    @Transactional(readOnly = true)
    public List<UnidadTerritorialSummaryDTO> findAll() {
        return unidadTerritorialRepository.findAll().stream()
                .map(this::mapToUnidadTerritorialSummaryDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<UnidadTerritorialEntity> findById(Integer id) {
        return unidadTerritorialRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<UnidadTerritorialResponseDTO> findByIdAsDto(Integer id) {
        return unidadTerritorialRepository.findById(id)
                .map(this::mapToUnidadTerritorialResponseDTO);
    }

    @Transactional
    public UnidadTerritorialResponseDTO createUnidadTerritorial(UnidadTerritorialRequestDTO requestDTO) {
        UnidadTerritorialEntity ut = new UnidadTerritorialEntity();
        ut.setCodigo(requestDTO.getCodigo());
        ut.setNombre(requestDTO.getNombre());
        ut.setComentario(requestDTO.getComentario());
        ut.setImagen(requestDTO.getImagen());
        // Asignar tipo y activo si se manejan en el requestDTO y la entidad
        if (requestDTO.getTipo() != null && !requestDTO.getTipo().isEmpty()) {
            ut.setTipo(requestDTO.getTipo().charAt(0));
        }
        ut.setActivo(requestDTO.getActivo());

        if (requestDTO.getDemarcacionId() != null) {
            DemarcacionEntity demarcacion = demarcacionRepository.findById(requestDTO.getDemarcacionId())
                    .orElseThrow(() -> new EntityNotFoundException("Demarcacion no encontrada con id: " + requestDTO.getDemarcacionId()));
            ut.setDemarcacion(demarcacion);
        }

        ut.setEstacionesUt(new HashSet<>());

        UnidadTerritorialEntity savedUt = unidadTerritorialRepository.save(ut);

        if (requestDTO.getEstacionesIds() != null && !requestDTO.getEstacionesIds().isEmpty()) {
            for (Integer estacionId : requestDTO.getEstacionesIds()) {
                EstacionEntity estacion = estacionRepository.findById(estacionId)
                        .orElseThrow(() -> new EntityNotFoundException("Estacion no encontrada con id: " + estacionId));
                EstacionUtEntity asociacion = new EstacionUtEntity();
                asociacion.setUnidadTerritorial(savedUt);
                asociacion.setEstacion(estacion);
                estacionUtRepository.save(asociacion);
                savedUt.getEstacionesUt().add(asociacion);
            }
        }
        return mapToUnidadTerritorialResponseDTO(savedUt);
    }

    @Transactional
    public UnidadTerritorialResponseDTO updateUnidadTerritorial(Integer id, UnidadTerritorialRequestDTO requestDTO) {
        UnidadTerritorialEntity ut = unidadTerritorialRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("UnidadTerritorial no encontrada con id: " + id));

        ut.setCodigo(requestDTO.getCodigo());
        ut.setNombre(requestDTO.getNombre());
        ut.setComentario(requestDTO.getComentario());
        ut.setImagen(requestDTO.getImagen());
        // Actualizar tipo y activo si se manejan en el requestDTO y la entidad
        if (requestDTO.getTipo() != null && !requestDTO.getTipo().isEmpty()) {
            ut.setTipo(requestDTO.getTipo().charAt(0));
        }
        ut.setActivo(requestDTO.getActivo());

        if (requestDTO.getDemarcacionId() != null) {
            DemarcacionEntity demarcacion = demarcacionRepository.findById(requestDTO.getDemarcacionId())
                    .orElseThrow(() -> new EntityNotFoundException("Demarcacion no encontrada con id: " + requestDTO.getDemarcacionId()));
            ut.setDemarcacion(demarcacion);
        } else {
            ut.setDemarcacion(null);
        }

        estacionUtRepository.deleteByUnidadTerritorialId(ut.getId());
        if (ut.getEstacionesUt() != null) {
            ut.getEstacionesUt().clear();
        } else {
            ut.setEstacionesUt(new HashSet<>());
        }

        unidadTerritorialRepository.flush();


        if (requestDTO.getEstacionesIds() != null && !requestDTO.getEstacionesIds().isEmpty()) {
            for (Integer estacionId : requestDTO.getEstacionesIds()) {
                EstacionEntity estacion = estacionRepository.findById(estacionId)
                        .orElseThrow(() -> new EntityNotFoundException("Estacion no encontrada con id: " + estacionId));
                EstacionUtEntity asociacion = new EstacionUtEntity();
                asociacion.setUnidadTerritorial(ut);
                asociacion.setEstacion(estacion);
                estacionUtRepository.save(asociacion);
                ut.getEstacionesUt().add(asociacion);
            }
        }

        UnidadTerritorialEntity updatedUt = unidadTerritorialRepository.save(ut);
        return mapToUnidadTerritorialResponseDTO(updatedUt);
    }

    @Transactional(readOnly = true)
    public List<UnidadTerritorialProjection> getUnidadesTerritorialesByTipo(Character tipo) {
        if (tipo == null) {
            throw new IllegalArgumentException("El tipo no puede ser nulo");
        }
        return unidadTerritorialRepository.findUnidadesTerritorialesByTipo(tipo);
    }
}
