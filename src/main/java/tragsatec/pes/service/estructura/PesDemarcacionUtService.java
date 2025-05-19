package tragsatec.pes.service.estructura;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tragsatec.pes.persistence.entity.estructura.PesDemarcacionUtEntity;
import tragsatec.pes.persistence.repository.estructura.PesDemarcacionUtRepository; // Asume que este repositorio existe

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PesDemarcacionUtService {

    private final PesDemarcacionUtRepository pesDemarcacionUtRepository;

    @Transactional(readOnly = true)
    public List<PesDemarcacionUtEntity> findAll() {
        return pesDemarcacionUtRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<PesDemarcacionUtEntity> findById(Integer id) {
        return pesDemarcacionUtRepository.findById(id);
    }

    @Transactional
    public PesDemarcacionUtEntity save(PesDemarcacionUtEntity pesDemarcacionUtEntity) {
        return pesDemarcacionUtRepository.save(pesDemarcacionUtEntity);
    }

    @Transactional
    public Optional<PesDemarcacionUtEntity> update(Integer id, PesDemarcacionUtEntity entityDetails) {
        return pesDemarcacionUtRepository.findById(id)
                .map(existingEntity -> {

                    if (entityDetails.getPes() != null) existingEntity.setPes(entityDetails.getPes());
                    if (entityDetails.getUnidadTerritorialId() != null)
                        existingEntity.setUnidadTerritorialId(entityDetails.getUnidadTerritorialId());
                    if (entityDetails.getDemarcacionId() != null)
                        existingEntity.setDemarcacionId(entityDetails.getDemarcacionId());
                    if (entityDetails.getCoeficiente() != null)
                        existingEntity.setCoeficiente(entityDetails.getCoeficiente());

                    return pesDemarcacionUtRepository.save(existingEntity);
                });
    }
}
