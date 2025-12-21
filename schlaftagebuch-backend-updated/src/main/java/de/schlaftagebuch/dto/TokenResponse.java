package de.schlaftagebuch.dto;

/**
 * Standard-Antwort nach erfolgreichem Login.
 * Enthält das JWT-Token und Metadaten zum angemeldeten Benutzer.
 */

public class TokenResponse {
    public String token; // JWT für nachfolgende API-Aufrufe
    public String role; // Rolle des Benutzers (ARZT oder PATIENT)
    public String subjectRef; // Referenz auf die FHIR-Ressource (z.B. Patient/...)
    public String username;  // Login-Name bzw. Patienten-Login-ID (optional für Clients)

    public TokenResponse(String token, String role, String subjectRef) {
        this(token, role, subjectRef, null);
    }

    public TokenResponse(String token, String role, String subjectRef, String username) {
        this.token = token;
        this.role = role;
        this.subjectRef = subjectRef;
        this.username = username;
    }
}
