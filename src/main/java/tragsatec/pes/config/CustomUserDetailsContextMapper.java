package tragsatec.pes.config;

import org.springframework.ldap.core.DirContextOperations;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.ldap.userdetails.LdapUserDetailsMapper;
import org.springframework.stereotype.Component;
import tragsatec.pes.persistence.entity.PermissionEntity;
import tragsatec.pes.persistence.entity.RoleEntity;
import tragsatec.pes.persistence.entity.UserEntity;
import tragsatec.pes.persistence.repository.UserRepository;


import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class CustomUserDetailsContextMapper extends LdapUserDetailsMapper {

    private final UserRepository userRepository;

    public CustomUserDetailsContextMapper(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails mapUserFromContext(DirContextOperations ctx, String username, Collection<? extends GrantedAuthority> authorities) {
        // Get basic UserDetails from LDAP (may not have roles or may have LDAP roles)
        UserDetails details = super.mapUserFromContext(ctx, username, authorities);

        // Find the user in your database
        UserEntity userEntity = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found in DB: " + username)); // Or handle this as you prefer

        // Get the unique role of the user from the database
        RoleEntity userRole = userEntity.getRole();
        if (userRole == null) {
            throw new RuntimeException("User found in DB but has no role assigned: " + username);
        }

        // Initialize the set for authorities from the database
        Set<GrantedAuthority> dbAuthorities = new HashSet<>();

        // Add the role as an authority.
        // Spring Security often expects the "ROLE_" prefix. Adjust if your configuration is different.
//        dbAuthorities.add(new SimpleGrantedAuthority("ROLE_" + userRole.getName())); // Assume ROLE_ prefix

        // Get the permissions associated with the role
        Set<PermissionEntity> permissions = userRole.getPermissions();
        if (permissions != null) {
            // Add each permission name as an authority
            permissions.stream()
                    .map(permission -> new SimpleGrantedAuthority(permission.getName())) // Use permission name as authority
                    .forEach(dbAuthorities::add);
        }


        // Create a new UserDetails combining information from LDAP and the DB authorities (role + permissions)
        // Use the LDAP password (details.getPassword())
        // Status flags (enabled, accountNonExpired, etc.) can come from LDAP or your DB (userEntity). Here we use the LDAP ones.
        return new User(details.getUsername(), details.getPassword(), details.isEnabled(),
                details.isAccountNonExpired(), details.isCredentialsNonExpired(),
                details.isAccountNonLocked(), dbAuthorities); // Use the combined authorities from the DB
    }
}