package de.schlaftagebuch.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Request-Body für spätere Logins des Patienten.
 */

public class PatientLoginRequest {

    @NotBlank
    public String username; // vom System generierte Patienten-Login-ID

    @NotBlank
    public String password;
}
