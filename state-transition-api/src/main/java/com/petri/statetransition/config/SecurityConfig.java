package com.petri.statetransition.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.MapReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Configuration de sécurité pour l'API réactive
 */
@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeExchange(exchanges -> exchanges
                        // Endpoints publics
                        .pathMatchers(HttpMethod.GET, "/actuator/health").permitAll()
                        .pathMatchers(HttpMethod.GET, "/api/v1/metrics/health").permitAll()

                        // Endpoints pour les viewers (lecture seule)
                        .pathMatchers(HttpMethod.GET, "/api/v1/services/**").hasAnyRole("ADMIN", "USER", "VIEWER")
                        .pathMatchers(HttpMethod.GET, "/api/v1/unit-resources/**").hasAnyRole("ADMIN", "USER", "VIEWER")
                        .pathMatchers(HttpMethod.GET, "/api/v1/composite-resources/**").hasAnyRole("ADMIN", "USER", "VIEWER")
                        .pathMatchers(HttpMethod.GET, "/api/v1/transitions/**").hasAnyRole("ADMIN", "USER", "VIEWER")
                        .pathMatchers(HttpMethod.GET, "/api/v1/metrics/**").hasAnyRole("ADMIN", "USER", "VIEWER")

                        // Endpoints pour les utilisateurs (lecture + actions de base)
                        .pathMatchers(HttpMethod.POST, "/api/v1/services").hasAnyRole("ADMIN", "USER")
                        .pathMatchers(HttpMethod.PUT, "/api/v1/services/**").hasAnyRole("ADMIN", "USER")
                        .pathMatchers(HttpMethod.POST, "/api/v1/services/*/start").hasAnyRole("ADMIN", "USER")
                        .pathMatchers(HttpMethod.POST, "/api/v1/services/*/complete").hasAnyRole("ADMIN", "USER")
                        .pathMatchers(HttpMethod.POST, "/api/v1/services/*/cancel").hasAnyRole("ADMIN", "USER")
                        .pathMatchers(HttpMethod.POST, "/api/v1/unit-resources/*/allocate").hasAnyRole("ADMIN", "USER")
                        .pathMatchers(HttpMethod.POST, "/api/v1/unit-resources/*/use").hasAnyRole("ADMIN", "USER")
                        .pathMatchers(HttpMethod.POST, "/api/v1/unit-resources/*/release").hasAnyRole("ADMIN", "USER")
                        .pathMatchers(HttpMethod.POST, "/api/v1/composite-resources/*/reserve").hasAnyRole("ADMIN", "USER")
                        .pathMatchers(HttpMethod.POST, "/api/v1/composite-resources/*/use").hasAnyRole("ADMIN", "USER")
                        .pathMatchers(HttpMethod.POST, "/api/v1/composite-resources/*/release").hasAnyRole("ADMIN", "USER")
                        .pathMatchers(HttpMethod.POST, "/api/v1/transitions/trigger").hasAnyRole("ADMIN", "USER")

                        // Endpoints pour les administrateurs seulement
                        .pathMatchers(HttpMethod.DELETE, "/api/v1/services/**").hasRole("ADMIN")
                        .pathMatchers(HttpMethod.POST, "/api/v1/unit-resources").hasRole("ADMIN")
                        .pathMatchers(HttpMethod.PUT, "/api/v1/unit-resources/**").hasRole("ADMIN")
                        .pathMatchers(HttpMethod.DELETE, "/api/v1/unit-resources/**").hasRole("ADMIN")
                        .pathMatchers(HttpMethod.POST, "/api/v1/composite-resources").hasRole("ADMIN")
                        .pathMatchers(HttpMethod.DELETE, "/api/v1/composite-resources/**").hasRole("ADMIN")
                        .pathMatchers(HttpMethod.POST, "/api/v1/transitions/*/cancel").hasRole("ADMIN")
                        .pathMatchers(HttpMethod.DELETE, "/api/v1/transitions/cleanup").hasRole("ADMIN")
                        .pathMatchers(HttpMethod.POST, "/api/v1/transitions/process-automatic").hasRole("ADMIN")
                        .pathMatchers("/api/v1/admin/**").hasRole("ADMIN")

                        // Endpoints Actuator
                        .pathMatchers("/actuator/**").hasRole("ADMIN")

                        // Tout le reste nécessite une authentification
                        .anyExchange().authenticated()
                )
                .httpBasic(basic -> {}) // Authentification HTTP Basic pour simplicité
                .build();
    }

    @Bean
    public MapReactiveUserDetailsService userDetailsService() {
        // Utilisateurs de test - En production, utiliser une base de données
        UserDetails admin = User.builder()
                .username("admin")
                .password(passwordEncoder().encode("admin123"))
                .roles("ADMIN")
                .build();

        UserDetails user = User.builder()
                .username("user")
                .password(passwordEncoder().encode("user123"))
                .roles("USER")
                .build();

        UserDetails viewer = User.builder()
                .username("viewer")
                .password(passwordEncoder().encode("viewer123"))
                .roles("VIEWER")
                .build();

        return new MapReactiveUserDetailsService(admin, user, viewer);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}