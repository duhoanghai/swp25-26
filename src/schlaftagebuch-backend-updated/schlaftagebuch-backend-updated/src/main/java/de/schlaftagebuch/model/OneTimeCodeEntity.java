package de.schlaftagebuch.model;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

/**
 * Datenbank-Entität für vom Arzt erzeugte Einmal-Codes.
 * Jeder Code ist genau einem FHIR-Patienten zugeordnet.
 */

@Entity
@Table(name = "one_time_code", indexes = {
        @Index(name = "idx_one_time_code_code", columnList = "code")
})
public class OneTimeCodeEntity {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false, length = 20, unique = true)
    private String code; // 6-stelliger Einmal-Code

    // "Patient/{uuid}"
    @Column(nullable = false, length = 120)
    private String patientRef; // 6-stelliger Einmal-Code

    @Column(nullable = false)
    private Instant expiresAt; // Ablaufzeitpunkt des Codes

    @Column
    private Instant usedAt; // Zeitpunkt der Verwendung, null = noch nicht benutzt

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getPatientRef() { return patientRef; }
    public void setPatientRef(String patientRef) { this.patientRef = patientRef; }

    public Instant getExpiresAt() { return expiresAt; }
    public void setExpiresAt(Instant expiresAt) { this.expiresAt = expiresAt; }

    public Instant getUsedAt() { return usedAt; }
    public void setUsedAt(Instant usedAt) { this.usedAt = usedAt; }

    // Hilfsmethoden:
    public boolean isUsed() { return usedAt != null; }
    public boolean isExpired(Instant now) { return expiresAt.isBefore(now); }
}
