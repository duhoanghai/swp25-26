package de.schlaftagebuch.arztManagement.dto;

import java.time.Instant;
import java.util.UUID;

/**
 * Antwort für das Erzeugen eines Einmal-Codes durch den Arzt.
 */
public class GenerateCodeResponse {
    public UUID id; // interne DB-ID des Codes (für Deaktivieren)
    public String code; // Einmal-Code, den der Patient beim ersten Login eingibt
    public Instant createdAt; // Zeitpunkt der Erstellung
    public Instant expiresAt; // Zeitpunkt, wann der Code abläuft
    public String patientId; // interne FHIR-Patienten-ID (für Debugging)
    public String patientRef; // z.B. "Patient/{id}"

    public GenerateCodeResponse() {}

    public GenerateCodeResponse(UUID id, String code, Instant createdAt, Instant expiresAt, String patientId, String patientRef) {
        this.id = id;
        this.code = code;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
        this.patientId = patientId;
        this.patientRef = patientRef;
    }

    // Backward-compat (falls Frontend noch alte Felder erwartet)
    public GenerateCodeResponse(String code, Instant expiresAt) {
        this(null, code, null, expiresAt, null, null);
    }
}
