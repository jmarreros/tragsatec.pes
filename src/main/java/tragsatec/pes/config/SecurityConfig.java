package tragsatec.pes.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {
    @Value("${ldap.url}")
    private String ldapUrl;

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable) // Disable CSRF for stateless APIs
                .authorizeHttpRequests((authorize) -> authorize
                        .requestMatchers("/login").permitAll() // Allow access to /login
                        .anyRequest().authenticated() // Require authentication for any other request
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // Use stateless sessions
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class); // Add JWT filter

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        // Ensure this encoder matches the one used in your LDAP if you store hashed passwords
        // The example LDIF uses BCrypt for 'ben', but plaintext for others.
        // If you only use direct LDAP comparison, this is not used directly in LDAP authentication,
        // but it's good practice to have it defined.
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http, PasswordEncoder passwordEncoder) throws Exception {
        AuthenticationManagerBuilder authenticationManagerBuilder = http.getSharedObject(AuthenticationManagerBuilder.class);
        authenticationManagerBuilder
                .ldapAuthentication()
                .userDnPatterns("uid={0},ou=people") // Pattern to find users
                .groupSearchBase("ou=groups") // Base for group search (optional if not using LDAP roles)
                .contextSource()
                .url(ldapUrl) // URL of the embedded LDAP server
                .and()
                .passwordCompare()
                .passwordEncoder(passwordEncoder) // Use the BCrypt encoder
                .passwordAttribute("userPassword"); // Password attribute in LDAP
        return authenticationManagerBuilder.build();
    }
}