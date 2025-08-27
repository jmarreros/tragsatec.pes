package com.chc.pes.config;

import com.chc.pes.dto.LdapAutentificacion;
import com.chc.pes.persistence.enums.UserRole;
import com.chc.pes.service.LdapAutentificacionService;
import com.chc.pes.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class CustomAuthenticationProvider implements AuthenticationProvider {

    private final UserDetailsService userDetailsService;
    private final UserService userService;
    private final LdapAutentificacionService ldapAuthService;

    @Value("${jwt.ldap.url}")
    private String ldapUrl;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String username = authentication.getName();
        String providedPassword = (String) authentication.getCredentials(); // Obtener contraseña

        // 1. Validate credentials against external service
        // --- Production Environment - LDAP ---
        if (!StringUtils.isEmpty(ldapUrl)) {
            LdapAutentificacion autentificacion = ldapAuthService.autentificacion(username, providedPassword);
            switch (autentificacion.getEstado()) {
                case DENEGADO:
                    throw new BadCredentialsException("User without permissions provided via external service");
                case ERROR:
                    throw new AuthenticationServiceException("Error during external authentication");
                case NO_ENCONTRADO:
                    throw new UsernameNotFoundException("Invalid credentials provided via external service");
                case OK:
                    break;
            }

            username = autentificacion.getUsuario();
            UserRole role = autentificacion.getRol();

            userService.insertOrUpdateUser(username, role.name());

            // Se debería crear el usuario en base de datos si no existe y si existe se
            // debería actualizar su rol

            // Para hacerlo compatible de momento se fuerza a que sea el usuario admin
            //username = ""; // TODO: quitar esto
        }

        // 2. Load user details from local database
        // --- Development Environment - Bypass external service - load user directly from local DB - Not required password check ---
        UserDetails userDetails;
        try {
            userDetails = userDetailsService.loadUserByUsername(username);

        } catch (UsernameNotFoundException e) {
            // Even if the external validation was OK, if the user does not exist locally,
            // it's a problem.
            // Or you could decide to create it here if that's your business logic.
            throw new BadCredentialsException("User authenticated externally but not found locally");
        }

        // 3. Check local account status (locked, disabled, etc.)
        if (!userDetails.isAccountNonLocked()) {
            throw new LockedException("User account is locked");
        }
        if (!userDetails.isEnabled()) {
            throw new DisabledException("User is disabled");
        }

        // 4. Create a new authentication token with the user details
        UsernamePasswordAuthenticationToken authenticatedToken = new UsernamePasswordAuthenticationToken(
                userDetails.getUsername(),
                null,
                userDetails.getAuthorities());
        authenticatedToken.setDetails(authentication.getDetails());
        return authenticatedToken;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}