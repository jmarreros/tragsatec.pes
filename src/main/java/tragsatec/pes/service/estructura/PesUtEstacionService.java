package tragsatec.pes.service.estructura;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tragsatec.pes.persistence.entity.estructura.PesUtEstacionEntity;
import tragsatec.pes.persistence.repository.estructura.PesUtEstacionRepository; // Asume que este repositorio existe

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PesUtEstacionService {

    private final PesUtEstacionRepository pesUtEstacionRepository;

    @Transactional(readOnly = true)
    public List<PesUtEstacionEntity> findAll() {
        return pesUtEstacionRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<PesUtEstacionEntity> findById(Integer id) {
        return pesUtEstacionRepository.findById(id);
    }

    @Transactional
    public PesUtEstacionEntity save(PesUtEstacionEntity pesUtEstacionEntity) {
        return pesUtEstacionRepository.save(pesUtEstacionEntity);
    }

    @Transactional
    public Optional<PesUtEstacionEntity> update(Integer id, PesUtEstacionEntity entityDetails) {
        return pesUtEstacionRepository.findById(id)
                .map(existingEntity -> {

                    if (entityDetails.getPes() != null) existingEntity.setPes(entityDetails.getPes());
                    if (entityDetails.getUnidadTerritorialId() != null) existingEntity.setUnidadTerritorialId(entityDetails.getUnidadTerritorialId());
                    if (entityDetails.getEstacionId() != null) existingEntity.setEstacionId(entityDetails.getEstacionId());
                    if (entityDetails.getCoeficiente() != null) existingEntity.setCoeficiente(entityDetails.getCoeficiente());

                    return pesUtEstacionRepository.save(existingEntity);
                });
    }

}
