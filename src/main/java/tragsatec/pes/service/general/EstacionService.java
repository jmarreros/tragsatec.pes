package tragsatec.pes.service.general;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tragsatec.pes.dto.general.EstacionRequestDTO;
import tragsatec.pes.dto.general.EstacionResponseDTO;
import tragsatec.pes.dto.general.UnidadTerritorialSummaryDTO;
import tragsatec.pes.persistence.entity.general.EstacionEntity;
import tragsatec.pes.persistence.entity.general.EstacionUtEntity;
import tragsatec.pes.persistence.entity.general.UnidadTerritorialEntity;
import tragsatec.pes.persistence.repository.general.EstacionRepository;
import tragsatec.pes.persistence.repository.general.EstacionUtRepository;
import tragsatec.pes.persistence.repository.general.UnidadTerritorialRepository;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class EstacionService {
    private final EstacionRepository estacionRepository;
    private final EstacionUtRepository estacionUtRepository;
    private final UnidadTerritorialRepository unidadTerritorialRepository;

    @Autowired
    public EstacionService(EstacionRepository estacionRepository,
                           EstacionUtRepository estacionUtRepository,
                           UnidadTerritorialRepository unidadTerritorialRepository) {
        this.estacionRepository = estacionRepository;
        this.estacionUtRepository = estacionUtRepository;
        this.unidadTerritorialRepository = unidadTerritorialRepository;
    }

    @Transactional(readOnly = true)
    public List<EstacionResponseDTO> findAll() {
        return estacionRepository.findAll().stream()
                .map(this::mapToEstacionResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<EstacionResponseDTO> findByIdAsDto(Integer id) {
        return estacionRepository.findById(id).map(this::mapToEstacionResponseDTO);
    }

    @Transactional
    public EstacionResponseDTO createEstacionAndUnidadesTerritoriales(EstacionRequestDTO dto) {
        if (dto.getCodigo() == null || dto.getCodigo().trim().isEmpty()) {
            throw new IllegalArgumentException("El código de la estación no puede ser nulo o vacío.");
        }
        if (estacionRepository.existsByCodigo(dto.getCodigo())) {
            throw new DataIntegrityViolationException("Ya existe una estación con el código: " + dto.getCodigo());
        }

        EstacionEntity nuevaEstacion = new EstacionEntity();
        nuevaEstacion.setCodigo(dto.getCodigo());
        nuevaEstacion.setNombre(dto.getNombre());
        nuevaEstacion.setTipo(dto.getTipo());
        nuevaEstacion.setFuente(dto.getFuente());
        nuevaEstacion.setCalidadDato(dto.getCalidadDato());
        nuevaEstacion.setCodigoSincronizacion(dto.getCodigoSincronizacion());
        nuevaEstacion.setProvincia(dto.getProvincia());
        nuevaEstacion.setActivo(dto.getActivo() != null ? dto.getActivo() : true);
        nuevaEstacion.setComentario(dto.getComentario());
        nuevaEstacion.setEstacionesUt(new HashSet<>());

        EstacionEntity estacionGuardada = estacionRepository.save(nuevaEstacion);

        if (dto.getUnidadTerritorialIds() != null && !dto.getUnidadTerritorialIds().isEmpty()) {
            for (Integer utId : dto.getUnidadTerritorialIds()) {
                UnidadTerritorialEntity ut = unidadTerritorialRepository.findById(utId)
                        .orElseThrow(() -> new EntityNotFoundException("UnidadTerritorial no encontrada con id: " + utId));

                EstacionUtEntity nuevaAsociacion = new EstacionUtEntity();
                nuevaAsociacion.setEstacion(estacionGuardada);
                nuevaAsociacion.setUnidadTerritorial(ut);
                estacionUtRepository.save(nuevaAsociacion);
                estacionGuardada.getEstacionesUt().add(nuevaAsociacion);
            }
        }
        return mapToEstacionResponseDTO(estacionGuardada);
    }

    public EstacionResponseDTO mapToEstacionResponseDTO(EstacionEntity estacion) {
        EstacionResponseDTO dto = new EstacionResponseDTO();
        dto.setId(estacion.getId());
        dto.setCodigo(estacion.getCodigo());
        dto.setNombre(estacion.getNombre());
        dto.setTipo(estacion.getTipo());
        dto.setFuente(estacion.getFuente());
        dto.setCalidadDato(estacion.getCalidadDato());
        dto.setCodigoSincronizacion(estacion.getCodigoSincronizacion());
        dto.setProvincia(estacion.getProvincia());
        dto.setActivo(estacion.getActivo());
        dto.setComentario(estacion.getComentario());

        if (estacion.getEstacionesUt() != null) {
            List<UnidadTerritorialSummaryDTO> utsDTOs = estacion.getEstacionesUt().stream()
                    .map(estacionUt -> {
                        UnidadTerritorialEntity ut = estacionUt.getUnidadTerritorial();
                        UnidadTerritorialSummaryDTO utDto = new UnidadTerritorialSummaryDTO();
                        utDto.setId(ut.getId());
                        utDto.setCodigo(ut.getCodigo());
                        utDto.setNombre(ut.getNombre());
                        return utDto;
                    })
                    .collect(Collectors.toList());
            dto.setUnidadesTerritoriales(utsDTOs);
        }
        return dto;
    }

    @Transactional
    public EstacionResponseDTO updateEstacionAndUnidadesTerritoriales(Integer estacionId, EstacionRequestDTO dto) {
        EstacionEntity estacion = estacionRepository.findById(estacionId)
                .orElseThrow(() -> new EntityNotFoundException("Estación no encontrada con id: " + estacionId));

        if (dto.getCodigo() != null && !dto.getCodigo().equals(estacion.getCodigo()) && estacionRepository.existsByCodigo(dto.getCodigo())) {
            throw new DataIntegrityViolationException("Ya existe otra estación con el código: " + dto.getCodigo());
        }
        if (dto.getCodigo() != null) estacion.setCodigo(dto.getCodigo());
        if (dto.getNombre() != null) estacion.setNombre(dto.getNombre());
        if (dto.getTipo() != null) estacion.setTipo(dto.getTipo());
        if (dto.getFuente() != null) estacion.setFuente(dto.getFuente());
        if (dto.getCalidadDato() != null) estacion.setCalidadDato(dto.getCalidadDato());
        if (dto.getCodigoSincronizacion() != null) estacion.setCodigoSincronizacion(dto.getCodigoSincronizacion());
        if (dto.getProvincia() != null) estacion.setProvincia(dto.getProvincia());
        if (dto.getActivo() != null) estacion.setActivo(dto.getActivo());
        if (dto.getComentario() != null) estacion.setComentario(dto.getComentario());


        estacionUtRepository.deleteByEstacionId(estacion.getId());
        if (estacion.getEstacionesUt() != null) {
            estacion.getEstacionesUt().clear();
        } else {
            estacion.setEstacionesUt(new HashSet<>());
        }

        estacionRepository.flush();

        if (dto.getUnidadTerritorialIds() != null && !dto.getUnidadTerritorialIds().isEmpty()) {
            for (Integer utId : dto.getUnidadTerritorialIds()) {
                UnidadTerritorialEntity ut = unidadTerritorialRepository.findById(utId)
                        .orElseThrow(() -> new EntityNotFoundException("UnidadTerritorial no encontrada con id: " + utId));

                EstacionUtEntity nuevaAsociacion = new EstacionUtEntity();
                nuevaAsociacion.setEstacion(estacion);
                nuevaAsociacion.setUnidadTerritorial(ut);
                estacionUtRepository.save(nuevaAsociacion); // La línea que seleccionaste
                estacion.getEstacionesUt().add(nuevaAsociacion);
            }
        }
        EstacionEntity estacionActualizada = estacionRepository.save(estacion);
        return mapToEstacionResponseDTO(estacionActualizada);
    }
}