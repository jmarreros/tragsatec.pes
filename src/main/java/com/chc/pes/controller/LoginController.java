package com.chc.pes.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority; // Import GrantedAuthority
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import com.chc.pes.config.JwtUtil;
import com.chc.pes.dto.LoginRequestDTO;

import java.util.Collection; // Import Collection
import java.util.Map;

@RestController
@Slf4j
public class LoginController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    public LoginController(AuthenticationManager authenticationManager, JwtUtil jwtUtil) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/api/v1/login")
    public ResponseEntity<?> login(@RequestBody LoginRequestDTO loginRequestDTO) {
        try {
            // Attempt authentication using the AuthenticationManager configured with LDAP
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequestDTO.getUsername(), loginRequestDTO.getPassword())
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
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Credenciales inválidas");
        }
    }
}