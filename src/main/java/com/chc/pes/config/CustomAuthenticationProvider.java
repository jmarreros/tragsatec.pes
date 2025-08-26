package com.chc.pes.config;

import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import com.chc.pes.service.ExternalAuthService;

@Component
public class CustomAuthenticationProvider implements AuthenticationProvider {

    private final UserDetailsService userDetailsService;
    private final ExternalAuthService externalAuthService;

    public CustomAuthenticationProvider(UserDetailsService userDetailsService,
                                        ExternalAuthService externalAuthService) {
        this.userDetailsService = userDetailsService;
        this.externalAuthService = externalAuthService;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String username = authentication.getName();
        String providedPassword = (String) authentication.getCredentials(); // Obtener contraseña

        // 1. Validate credentials against external service
        boolean isValidExternal = externalAuthService.validateCredentials(username, providedPassword);

        if (!isValidExternal) {
            throw new BadCredentialsException("Invalid credentials provided via external service");
        }

        // 2. Load user details from local database
        UserDetails userDetails;
        try {
            userDetails = userDetailsService.loadUserByUsername(username);
        } catch (UsernameNotFoundException e) {
            // Even if the external validation was OK, if the user does not exist locally, it's a problem.
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
                userDetails.getAuthorities()
        );
        authenticatedToken.setDetails(authentication.getDetails());
        return authenticatedToken;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}