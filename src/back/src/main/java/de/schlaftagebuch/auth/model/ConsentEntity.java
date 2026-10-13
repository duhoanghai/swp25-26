package de.schlaftagebuch.auth.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "consent")
public class ConsentEntity {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "consent_id", nullable = false, unique = true)
    private String consentId; // FHIR ID

    @Column(name = "patient_id", nullable = false)
    private String patientId; // z.B. "abc-123"

    @Column(name = "status", nullable = false)
    private String status; // "active", "rejected", etc.

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "fhir_json", columnDefinition = "TEXT")
    private String fhirJson; // Vollständiger FHIR Consent als JSON

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }

    // Getters & Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getConsentId() { return consentId; }
    public void setConsentId(String consentId) { this.consentId = consentId; }

    public String getPatientId() { return patientId; }
    public void setPatientId(String patientId) { this.patientId = patientId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public String getFhirJson() { return fhirJson; }
    public void setFhirJson(String fhirJson) { this.fhirJson = fhirJson; }
}
