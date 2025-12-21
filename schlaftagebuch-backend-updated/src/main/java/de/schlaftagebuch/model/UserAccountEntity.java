package de.schlaftagebuch.model;

import jakarta.persistence.*;
import java.util.UUID;

/**
 * Datenbank-Entität für Benutzerkonten (Arzt und Patient).
 * Verknüpft technische Zugangsdaten mit der jeweiligen FHIR-Ressource.
 */

@Entity
@Table(name = "user_account", uniqueConstraints = {
        @UniqueConstraint(name = "uk_user_account_username", columnNames = "username")
})
public class UserAccountEntity {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false, length = 100)
    private String username; // Login-Name (z.B. "arzt1" oder Patienten-Login-ID)

    @Column(nullable = false, length = 200)
    private String passwordHash; // BCrypt-Hash des Passworts

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role; // ARZT oder PATIENT

    // "Patient/{uuid}" oder "Practitioner/{uuid}"
    @Column(nullable = false, length = 120)
    private String subjectRef;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public String getSubjectRef() { return subjectRef; }
    public void setSubjectRef(String subjectRef) { this.subjectRef = subjectRef; }
}
