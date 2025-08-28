package com.chc.pes.config;

import com.chc.pes.dto.LdapAutentificacion;
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
        String providedPassword = (String) authentication.getCredentials();
        UserDetails userDetails;

        // 1. Validate credentials against external service
        if (!StringUtils.isBlank(ldapUrl)) {
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

            // Insert or update user in local database
            userService.insertOrUpdateUser(autentificacion.getUsuario(), autentificacion.getRol().name());

            username = autentificacion.getUsuario();
        }

        // 2. Load user details from local database (para entorno de desarrollo)
        userDetails = userDetailsService.loadUserByUsername(username);

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