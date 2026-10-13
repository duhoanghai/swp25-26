package de.schlaftagebuch.arztManagement.model;

import jakarta.persistence.*;

import java.time.Instant;
import java.time.LocalDate;
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

    @Column(nullable = false)
    private Instant createdAt;

    @Column
    private Instant usedAt; // Zeitpunkt der Verwendung, null = noch nicht benutzt

    @Column
    private Instant revokedAt;

    @Column(name = "appointment_date")
    private LocalDate appointmentDate;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getPatientRef() { return patientRef; }
    public void setPatientRef(String patientRef) { this.patientRef = patientRef; }

    public Instant getExpiresAt() { return expiresAt; }
    public void setExpiresAt(Instant expiresAt) { this.expiresAt = expiresAt; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUsedAt() { return usedAt; }
    public void setUsedAt(Instant usedAt) { this.usedAt = usedAt; }

    public Instant getRevokedAt() { return revokedAt; }
    public void setRevokedAt(Instant revokedAt) { this.revokedAt = revokedAt; }

    public LocalDate getAppointmentDate() { return appointmentDate; }
    public void setAppointmentDate(LocalDate appointmentDate) { this.appointmentDate = appointmentDate; }

    // Hilfsmethoden:
    public boolean isUsed() { return usedAt != null; }
    public boolean isExpired(Instant now) { return expiresAt.isBefore(now); }
    public boolean isRevoked() { return revokedAt != null; }
    public boolean isActive(Instant now) { return !isUsed() && !isExpired(now) && !isRevoked(); }
}
