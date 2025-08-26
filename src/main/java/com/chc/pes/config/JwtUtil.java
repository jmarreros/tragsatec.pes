package com.chc.pes.config;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.annotation.PostConstruct; // Import PostConstruct
import org.springframework.beans.factory.annotation.Value; // Import Value
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class JwtUtil {

    //Inject the secret key from application properties
    @Value("${jwt.secret.key}")
    private String secretKey;

    // Inject the expiration time from application properties
    @Value("${jwt.expiration.time.hours}")
    private int expirationTimeHours;

    // Declare Algorithm and Verifier without initializing here
    private Algorithm algorithm;
    private JWTVerifier verifier;

    private static final String AUTHORITIES_CLAIM = "authorities"; // Claim key for authorities

    // Initialize Algorithm and Verifier after properties are injected
    @PostConstruct
    public void init() {
        // Optional: Add validation for the key length
        if (secretKey == null || secretKey.getBytes().length * 8 < 256) {
            throw new RuntimeException("JWT Secret Key must be at least 256 bits long. Check your application properties.");
        }
        this.algorithm = Algorithm.HMAC256(secretKey);
        this.verifier = JWT.require(algorithm).build();
    }

    // Updated to accept authorities
    public String generateToken(String username, Collection<? extends GrantedAuthority> authorities) {
        List<String> authoritiesList = authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        return JWT.create()
                .withSubject(username)
                .withClaim(AUTHORITIES_CLAIM, authoritiesList) // Add authorities claim
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * expirationTimeHours))
                .sign(algorithm); // Use the initialized algorithm
    }

    public boolean validateToken(String token) {
        try {
            verifier.verify(token); // Use the initialized verifier
            return true;
        } catch (JWTVerificationException exception) {
            // Invalid token (expired, incorrect signature, etc.)
            return false;
        }
    }

    public String extractUsername(String token) {
        try {
            DecodedJWT decodedJWT = verifier.verify(token); // Use the initialized verifier
            return decodedJWT.getSubject();
        } catch (JWTVerificationException exception) {
            // Handle the exception if necessary
            return null;
        }
    }

    // New method to extract authorities
    public List<GrantedAuthority> extractAuthorities(String token) {
        try {
            DecodedJWT decodedJWT = verifier.verify(token); // Use the initialized verifier
            Claim authoritiesClaim = decodedJWT.getClaim(AUTHORITIES_CLAIM);
            if (authoritiesClaim.isNull() || authoritiesClaim.isMissing()) {
                return List.of(); // Return empty list if claim is not present
            }
            List<String> authoritiesList = authoritiesClaim.asList(String.class);
            return authoritiesList.stream()
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList());
        } catch (JWTVerificationException exception) {
            // Handle the exception if necessary
            return List.of();
        }
    }
}