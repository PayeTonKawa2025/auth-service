package org.payetonkawa.auth.auth_service.config;

import org.payetonkawa.auth.auth_service.security.JwtAuthFilter;
import org.payetonkawa.auth.auth_service.security.JwtService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private final JwtService jwtService;

    public SecurityConfig(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                // Désactivation CSRF OK : API REST stateless avec JWT transmis via header Authorization
                .csrf(AbstractHttpConfigurer::disable)
                .cors()  // Active la gestion du CORS dans Spring Security (prend la config globale)
                .and()
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/auth/login",
                                "/api/auth/register",
                                "/api/auth/refresh-token",
                                "/api/auth/logout"
                        ).permitAll()
                        .requestMatchers("/api/auth/roles/**").hasRole("ADMIN")  // Autorise l'accès aux rôles uniquement pour les ADMIN
                        .requestMatchers("/api/auth/users/*/roles").hasRole("ADMIN")  // Autorise la gestion des rôles utilisateurs uniquement pour les ADMIN
                        .requestMatchers("/api/auth/users").hasRole("ADMIN")  // Autorise les ADMIN à accéder à la liste des utilisateurs

                        .anyRequest().authenticated()
                )
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(new JwtAuthFilter(jwtService), UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
