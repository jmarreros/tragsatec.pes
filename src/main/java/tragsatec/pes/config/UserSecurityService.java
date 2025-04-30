package tragsatec.pes.config;

import org.springframework.security.core.authority.SimpleGrantedAuthority; // Importar
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import tragsatec.pes.persistence.entity.UserEntity;
import tragsatec.pes.persistence.repository.UserRepository;

import java.util.Collections; // Importar

@Service
public class UserSecurityService implements UserDetailsService {
    private final UserRepository userRepository;

    public UserSecurityService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserEntity userEntity = this.userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        return User.builder()
                .username(userEntity.getUsername())
                .password("")
                .disabled(userEntity.getLocked())
                .accountExpired(false)
                .credentialsExpired(false)
                .accountLocked(userEntity.getLocked())
                .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + userEntity.getRole().name())))
                .build();
    }
}