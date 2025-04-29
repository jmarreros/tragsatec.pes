package tragsatec.pes.controller;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority; // Import GrantedAuthority
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import tragsatec.pes.config.JwtUtil;
import tragsatec.pes.dto.LoginRequest;

import java.lang.reflect.Array;
import java.util.Collection; // Import Collection
import java.util.List;
import java.util.Map;

@RestController
public class AccessController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    public AccessController(AuthenticationManager authenticationManager, JwtUtil jwtUtil) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        try {
            // Attempt authentication using the AuthenticationManager configured with LDAP
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword())
            );

            // If authentication is successful, get username and authorities
            String username = authentication.getName();

            Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities(); // Get authorities

            // Generate the JWT token including authorities
            String token = jwtUtil.generateToken(username, authorities); // Pass authorities to generateToken

            // Return the token
            return ResponseEntity.ok(Map.of("token", token));
        } catch (AuthenticationException e) {
            // If authentication fails, return an error
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Error: Invalid credentials");
        }
    }
}