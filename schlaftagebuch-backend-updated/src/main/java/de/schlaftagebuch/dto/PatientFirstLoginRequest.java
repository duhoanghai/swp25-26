package de.schlaftagebuch.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Request-Body für den ersten Login des Patienten mit Einmal-Code.
 */

public class PatientFirstLoginRequest {
    @NotBlank
    public String code; // vom Arzt erzeugter Einmal-Code (6-stellig)

    @NotNull
    public Boolean consentAccepted; // Patient muss der Datenverarbeitung zustimmen

    @NotBlank
    public String givenName; // Vorname

    @NotBlank
    public String familyName; // Nachname

    // Entweder komplettes Geburtsdatum (YYYY-MM-DD) oder nur Geburtsjahr
    public String birthDate; // optional

    public Integer birthYear; // optional

    @NotBlank
    public String password; // selbst gewähltes Login-Passwort des Patienten
}
