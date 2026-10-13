package de.schlaftagebuch.auth.dto;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "patient")
public class PatientEntity {

    @Id
    @GeneratedValue
    private UUID id; // Interne ID

    @Column(name = "patient_id", nullable = false, unique = true, length = 100)
    private String patientId; // FHIR ID z.B. "abc-123"

    @Column(name = "user_account_id")
    private UUID userAccountId; // Verweis auf UserAccount

    @Column(name = "given_name", length = 100)
    private String givenName; // Vorname

    @Column(name = "family_name", length = 100)
    private String familyName; // Nachname

    @Column(name = "birth_date")
    private LocalDate birthDate; // Geburtsdatum

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Pre-Persist und Pre-Update
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Getter und Setter
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getPatientId() { return patientId; }
    public void setPatientId(String patientId) { this.patientId = patientId; }

    public UUID getUserAccountId() { return userAccountId; }
    public void setUserAccountId(UUID userAccountId) { this.userAccountId = userAccountId; }

    public String getGivenName() { return givenName; }
    public void setGivenName(String givenName) { this.givenName = givenName; }

    public String getFamilyName() { return familyName; }
    public void setFamilyName(String familyName) { this.familyName = familyName; }

    public LocalDate getBirthDate() { return birthDate; }
    public void setBirthDate(LocalDate birthDate) { this.birthDate = birthDate; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
