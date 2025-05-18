package tragsatec.pes.service.general;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tragsatec.pes.dto.UnidadTerritorialRequestDTO;
import tragsatec.pes.persistence.entity.general.DemarcacionEntity;
import tragsatec.pes.persistence.entity.general.UnidadTerritorialEntity;
import tragsatec.pes.persistence.repository.general.DemarcacionRepository;
import tragsatec.pes.persistence.repository.general.UnidadTerritorialRepository;

import java.util.List;
import java.util.Optional;

@Service
public class UnidadTerritorialService {
    private final UnidadTerritorialRepository unidadTerritorialRepository;
    private final DemarcacionRepository demarcacionRepository;

    @Autowired
    public UnidadTerritorialService(UnidadTerritorialRepository unidadTerritorialRepository, DemarcacionRepository demarcacionRepository) {
        this.unidadTerritorialRepository = unidadTerritorialRepository;
        this.demarcacionRepository = demarcacionRepository;
    }

    public List<UnidadTerritorialEntity> findAll() {
        return (List<UnidadTerritorialEntity>) unidadTerritorialRepository.findAll();
    }

    public Optional<UnidadTerritorialEntity> findById(Integer id) {
        return unidadTerritorialRepository.findById(id);
    }

    public UnidadTerritorialEntity save(UnidadTerritorialEntity entity) {
        return unidadTerritorialRepository.save(entity);
    }

    public UnidadTerritorialEntity createUnidadTerritorial(UnidadTerritorialRequestDTO dto) {
        UnidadTerritorialEntity ut = new UnidadTerritorialEntity();
        ut.setCodigo(dto.getCodigo());
        ut.setNombre(dto.getNombre());
        ut.setTipo(dto.getTipo());
        ut.setActivo(dto.getActivo());
        ut.setComentario(dto.getComentario());

        if (dto.getDemarcacion() != null) {
            DemarcacionEntity demarcacion = demarcacionRepository.findById(dto.getDemarcacion())
                    .orElseThrow(() -> new EntityNotFoundException("Demarcación no encontrada con id: " + dto.getDemarcacion()));
            ut.setDemarcacion(demarcacion);
        } else {
            throw new IllegalArgumentException("El ID de la demarcación no puede ser nulo.");
        }

        return unidadTerritorialRepository.save(ut);
    }

    public UnidadTerritorialEntity updateUnidadTerritorial(Integer id, UnidadTerritorialRequestDTO dto) {
        UnidadTerritorialEntity ut = unidadTerritorialRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("UnidadTerritorial no encontrada con id: " + id));

        ut.setNombre(dto.getNombre());
        ut.setTipo(dto.getTipo());
        ut.setActivo(dto.getActivo());
        ut.setComentario(dto.getComentario());

        if (dto.getDemarcacion() != null) {
            DemarcacionEntity demarcacion = demarcacionRepository.findById(dto.getDemarcacion())
                    .orElseThrow(() -> new EntityNotFoundException("Demarcación no encontrada con id: " + dto.getDemarcacion()));
            ut.setDemarcacion(demarcacion);
        } else {
            throw new IllegalArgumentException("El ID de la demarcación no puede ser nulo para la actualización si la demarcación es obligatoria.");
        }

        return unidadTerritorialRepository.save(ut);
    }

}

