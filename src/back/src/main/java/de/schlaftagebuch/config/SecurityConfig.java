package de.schlaftagebuch.config;

import de.schlaftagebuch.security.JwtAuthFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import static org.springframework.security.config.Customizer.withDefaults;
import java.util.List;
/**
 * Globale Sicherheitskonfiguration der REST-API.
 * - JWT-basierte Authentifizierung (stateless)
 * - Öffentliche Endpunkte für Login
 * - Rollenbasierter Zugriff für Arzt/Patient
 */
@EnableWebSecurity
@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthFilter jwtAuthFilter) throws Exception {
        http.cors(cors -> cors.configurationSource(corsConfigurationSource())) ; // CORS zuerst!
        // CSRF deaktivieren – wir haben eine reine REST-API ohne Browser-Formulare
        http.csrf(AbstractHttpConfigurer::disable);

        //wichtig für Website
        http.cors(withDefaults());

        // Stateless: keine HTTP-Session, Authentifizierung ausschließlich über JWT
        http.sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        // Keine HTTP-Basic- oder Formular-Logins, nur Token-Authentifizierung
        http.httpBasic(AbstractHttpConfigurer::disable);
        http.formLogin(AbstractHttpConfigurer::disable);

        http.authorizeHttpRequests(auth -> auth
                // Public auth endpoints
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .requestMatchers("/api/auth/arzt/login").permitAll()
                .requestMatchers("/api/auth/patient/first-login").permitAll()
                .requestMatchers("/api/auth/patient/login").permitAll()

                // FHIR-Endpunkte vorläufig für Tests freigegeben
                .requestMatchers("/fhir/**").permitAll()

                //Rechte für Website freigeben
                .requestMatchers(org.springframework.http.HttpMethod.OPTIONS, "/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/arzt/patients").hasRole("ARZT")
                .requestMatchers(HttpMethod.GET, "/api/arzt/codes").hasRole("ARZT")
                .requestMatchers(HttpMethod.POST, "/api/arzt/codes/*/deactivate").hasRole("ARZT")
                .requestMatchers(HttpMethod.GET, "/api/arzt/stats").hasRole("ARZT")
                .requestMatchers(HttpMethod.GET, "/api/arzt/patient-reports").hasRole("ARZT")
                .requestMatchers(HttpMethod.GET, "/api/arzt/patients/*/details").hasRole("ARZT")



                // Einmal-Code darf nur von Ärzten erzeugt werden
                .requestMatchers(HttpMethod.POST, "/api/auth/patients/*/generate-code").hasRole("ARZT")
                .requestMatchers(HttpMethod.POST, "/api/auth/patients/generate-code").hasRole("ARZT")
                //// Submit: nur PATIENT
                .requestMatchers(HttpMethod.POST, "/api/protocols/submissions").hasRole("PATIENT")
                //// Patient liest seine eigenen Submissions
                .requestMatchers(HttpMethod.GET, "/api/protocols/submissions/mine").hasRole("PATIENT")
                //// (optional) allgemeines Lesen:
                .requestMatchers(HttpMethod.GET, "/api/protocols/**").hasAnyRole("PATIENT", "ARZT")

                // Reports
                // - Patient darf nur seinen eigenen Report (inkl. Tages-PDFs) abrufen
                // - Arzt darf Reports nur über /patient/{patientId} abrufen
                .requestMatchers(HttpMethod.GET, "/api/reports/me/**").hasAnyRole("PATIENT", "ARZT")
                .requestMatchers(HttpMethod.GET, "/api/reports/patient/**").hasRole("ARZT")

                // Alle übrigen Endpunkte benötigen ein gültiges JWT
                .anyRequest().authenticated()
        );

        http.headers(h -> h.frameOptions(f -> f.disable()));
        http.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

//    //wichtig für Website, damit sie von jedem Gerät geöffnet werden kann
//    @Bean
//    public CorsConfigurationSource corsConfigurationSource() {
//        CorsConfiguration config = new CorsConfiguration();
//        config.setAllowedOrigins(List.of("http://localhost:5500"));
//        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
//        config.setAllowedHeaders(List.of("*"));
//        config.setAllowCredentials(false); // du nutzt Cookie-Auth nicht, daher false ok
//
//        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
//        source.registerCorsConfiguration("/**", config);
//        return source;
//    }


// NUR ZUM TESTEN - Oben ist die originale implementierung!!!
@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration config = new CorsConfiguration();
    config.setAllowedOrigins(List.of("*"));  // ALLE erlauben
    config.setAllowedMethods(List.of("*"));
    config.setAllowedHeaders(List.of("*"));
    // config.setAllowCredentials(true); // Kann nicht mit "*" kombiniert werden

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", config);
    return source;
}
}

