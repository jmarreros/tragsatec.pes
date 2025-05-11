package tragsatec.pes.persistence.repository;

import org.springframework.data.repository.CrudRepository;
import tragsatec.pes.persistence.entity.FileMeasurement;

public interface FileMeasurementRepository extends CrudRepository<FileMeasurement, Integer> {
}