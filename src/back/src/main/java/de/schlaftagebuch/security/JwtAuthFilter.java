package de.schlaftagebuch.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Filter, der das JWT aus dem Authorization-Header liest
 * und bei gültigem Token den Benutzer im SecurityContext anmeldet.
 */

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    public JwtAuthFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        System.out.println("=== JWT FILTER HIT === " + request.getMethod() + " " + request.getRequestURI());

        String header = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7).trim();

            try {
                Claims claims = jwtService.parse(token);

                String role = claims.get("role", String.class);
                String subjectRef = claims.getSubject();          // Patient/<uuid>
                String username = claims.get("username", String.class);

                var auth = new UsernamePasswordAuthenticationToken(
                        subjectRef,                               // principal
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_" + role))
                );
                auth.setDetails(username);

                System.out.println("JWT OK: role=" + role + ", username=" + username + ", sub=" + subjectRef);

                SecurityContextHolder.getContext().setAuthentication(auth);

            } catch (Exception e) {
                System.out.println("JWT PARSE FAILED: " + e.getClass().getName() + " - " + e.getMessage());
                e.printStackTrace();
                SecurityContextHolder.clearContext();
            }
        }

        filterChain.doFilter(request, response);
    }
}