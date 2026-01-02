package com.chc.pes.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider; // Import
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final AuthenticationProvider customAuthenticationProvider; // Inject the custom provider

    // Modify constructor to receive the provider
    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter,
                          AuthenticationProvider customAuthenticationProvider) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.customAuthenticationProvider = customAuthenticationProvider;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.cors(withDefaults()) // Configuración de CORS
                .csrf(AbstractHttpConfigurer::disable) // Desactiva CSRF para APIs
                .authorizeHttpRequests(authorize -> authorize
                        // Recursos públicos (frontend)
                        .requestMatchers("/", "/index.html", "/assets/**", "/app-config.js", "/css/**", "/js/**", "/img/**").permitAll()
                        // Login: permitir POST y OPTIONS
                        .requestMatchers(HttpMethod.OPTIONS, "/api/v1/login").permitAll().requestMatchers(HttpMethod.POST, "/api/v1/login").permitAll()
                        // Información de la aplicación: permitir GET
                        .requestMatchers("/api/v1/app/info").permitAll()
                        // Cualquier otra petición requiere autenticación
                        .anyRequest().authenticated())
                // Stateless session (JWT)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // Registra tu proveedor de autenticación personalizado
                .authenticationProvider(customAuthenticationProvider)
                // Añade el filtro JWT antes de UsernamePasswordAuthenticationFilter
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        // Although not used for comparison in your provider,
        // it's good practice to have it defined if other parts of Spring Security expect it.
        return new BCryptPasswordEncoder();
    }

    // This AuthenticationManager bean will now use the provider configured in HttpSecurity
    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder authenticationManagerBuilder =
                http.getSharedObject(AuthenticationManagerBuilder.class);
        // The provider is already registered in http, so the builder will pick it up.
        // Optionally, you could configure it explicitly here as well if necessary:
        // authenticationManagerBuilder.authenticationProvider(customAuthenticationProvider);
        return authenticationManagerBuilder.build();
    }
}