package de.schlaftagebuch.arztManagement.dto;

/**
 * Response DTO für Patientenliste (Arzt-GUI).
 *
 * Kombiniert Daten aus:
 * - UserAccount (username, subjectRef)
 * - FHIR Patient (givenName, familyName, gender, birthDate)
 */
public class PatientSummaryResponse {

    /**
     * Patient-ID (UUID ohne "Patient/" Prefix)
     */
    public String patientId;

    /**
     * FHIR SubjectRef (z.B. "Patient/abc-123")
     */
    public String subjectRef;

    /**
     * Login-Username (aus UserAccount)
     */
    public String username;

    /**
     * Vorname (aus FHIR Patient)
     */
    public String givenName;

    /**
     * Nachname (aus FHIR Patient)
     */
    public String familyName;

    /**
     * Geschlecht (aus FHIR Patient)
     * Werte: "male", "female", "other", "unknown"
     */
    public String gender;

    /**
     * Geburtsdatum (aus FHIR Patient)
     * Format: "YYYY-MM-DD"
     */
    public String birthDate;

    /**
     * Lesbarer Display-Name für UI
     * Z.B. "Müller, Anna" oder "patient123"
     */
    public String displayName;

    // === Constructors ===

    public PatientSummaryResponse() {
    }

    public PatientSummaryResponse(String patientId, String subjectRef, String username, String displayName) {
        this.patientId = patientId;
        this.subjectRef = subjectRef;
        this.username = username;
        this.displayName = displayName;
    }

    // === Getters & Setters ===

    public String getPatientId() {
        return patientId;
    }

    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }

    public String getSubjectRef() {
        return subjectRef;
    }

    public void setSubjectRef(String subjectRef) {
        this.subjectRef = subjectRef;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getGivenName() {
        return givenName;
    }

    public void setGivenName(String givenName) {
        this.givenName = givenName;
    }

    public String getFamilyName() {
        return familyName;
    }

    public void setFamilyName(String familyName) {
        this.familyName = familyName;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(String birthDate) {
        this.birthDate = birthDate;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
}
