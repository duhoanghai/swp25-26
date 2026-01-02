package de.schlaftagebuch.dto;

import java.time.Instant;

/**
 * Antwort für das Erzeugen eines Einmal-Codes durch den Arzt.
 */

public class GenerateCodeResponse {
    public String code; // Einmal-Code, den der Patient beim ersten Login eingibt
    public Instant expiresAt; // Zeitpunkt, wann der Code abläuft
    public String patientId; // interne FHIR-Patienten-ID (für Debugging)
    public String patientRef; // z.B. "Patient/{id}"

    public GenerateCodeResponse(String code, Instant expiresAt) {
        this(code, expiresAt, null, null);
    }

    public GenerateCodeResponse(String code, Instant expiresAt, String patientId, String patientRef) {
        this.code = code;
        this.expiresAt = expiresAt;
        this.patientId = patientId;
        this.patientRef = patientRef;
    }
}