package de.schlaftagebuch.config;

import de.schlaftagebuch.security.JwtAuthFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Globale Sicherheitskonfiguration der REST-API.
 * - JWT-basierte Authentifizierung (stateless)
 * - Öffentliche Endpunkte für Login
 * - Rollenbasierter Zugriff für Arzt/Patient
 */

@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthFilter jwtAuthFilter) throws Exception {

        // CSRF deaktivieren – wir haben eine reine REST-API ohne Browser-Formulare
        http.csrf(AbstractHttpConfigurer::disable);

        // Stateless: keine HTTP-Session, Authentifizierung ausschließlich über JWT
        http.sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        // Keine HTTP-Basic- oder Formular-Logins, nur Token-Authentifizierung
        http.httpBasic(AbstractHttpConfigurer::disable);
        http.formLogin(AbstractHttpConfigurer::disable);

        http.authorizeHttpRequests(auth -> auth
                // Public auth endpoints
                .requestMatchers("/api/auth/arzt/login").permitAll()
                .requestMatchers("/api/auth/patient/first-login").permitAll()
                .requestMatchers("/api/auth/patient/login").permitAll()

                // FHIR-Endpunkte vorläufig für Tests freigegeben
                .requestMatchers("/fhir/**").permitAll()

                // Einmal-Code darf nur von Ärzten erzeugt werden
                .requestMatchers(HttpMethod.POST, "/api/auth/patients/*/generate-code").hasRole("ARZT")
                .requestMatchers(HttpMethod.POST, "/api/auth/patients/generate-code").hasRole("ARZT")
                //// Submit: nur PATIENT
                .requestMatchers(HttpMethod.POST, "/api/protocols/submissions").hasRole("PATIENT")
                //// Patient liest seine eigenen Submissions
                .requestMatchers(HttpMethod.GET, "/api/protocols/submissions/mine").hasRole("PATIENT")
                //// (optional) allgemeines Lesen:
                .requestMatchers(HttpMethod.GET, "/api/protocols/**").hasAnyRole("PATIENT", "ARZT")

                .requestMatchers("/h2-console/**").permitAll()

                // Alle übrigen Endpunkte benötigen ein gültiges JWT
                .anyRequest().authenticated()
        );

        http.headers(h -> h.frameOptions(f -> f.disable()));
        http.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
