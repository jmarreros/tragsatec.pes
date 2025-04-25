package tragsatec.pes.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tragsatec.pes.dto.RoleDto;
import tragsatec.pes.persistence.entity.RoleEntity;
import tragsatec.pes.persistence.repository.RoleRepository;

import java.util.List;
import java.util.stream.StreamSupport;

@Service
public class RoleService {
    private final RoleRepository roleRepository;

    @Autowired
    public RoleService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    public List<RoleDto> getAllRoles() {
        return StreamSupport.stream(roleRepository.findAll().spliterator(), false)
                .map(role -> new RoleDto(role.getId(), role.getName()))
                .toList();
    }

    public List<RoleEntity> getAllRolesWithPermissions() {
        return (List<RoleEntity>) roleRepository.findAll(); // `permissions` se cargará automáticamente por `FetchType.EAGER`.
    }
}
