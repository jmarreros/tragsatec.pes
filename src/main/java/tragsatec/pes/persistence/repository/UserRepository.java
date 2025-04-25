package tragsatec.pes.persistence.repository;

import org.springframework.data.repository.CrudRepository;
import tragsatec.pes.persistence.entity.UserEntity;

import java.util.Optional;

public interface UserRepository extends CrudRepository<UserEntity, Integer> {
    Optional<UserEntity> findByUsername(String username);
}
