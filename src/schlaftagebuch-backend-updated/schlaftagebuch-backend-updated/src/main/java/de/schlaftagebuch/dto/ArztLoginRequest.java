package de.schlaftagebuch.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Request-Body für die Anmeldung des Arztes.
 */

public class ArztLoginRequest {
    @NotBlank
    public String username; // z.B. "arzt1"

    @NotBlank
    public String password; // Klartext-Passwort, wird im Service mit dem Hash verglichen
}
