package tragsatec.pes.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tragsatec.pes.dto.RoleDto;
import tragsatec.pes.persistence.entity.RoleEntity;
import tragsatec.pes.service.RoleService;

import java.util.List;

@RestController
@RequestMapping("roles")
public class RoleController {
    private final RoleService roleService;

    @Autowired
    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @GetMapping
    public ResponseEntity<List<RoleDto>> getAllRoles() {
        List<RoleDto> roles = roleService.getAllRoles();
        return ResponseEntity.ok(roles);
    }

    @GetMapping("/with-permissions")
    public ResponseEntity<List<RoleEntity>> getRolesWithPermissions() {
        List<RoleEntity> roles = roleService.getAllRolesWithPermissions();
        return ResponseEntity.ok(roles);
    }
}
