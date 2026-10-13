package de.schlaftagebuch.arztManagement.dto;

import jakarta.validation.constraints.Size;

/**
 * Request für das Erzeugen eines Einmal-Codes.
 *
 * validUntilDate: ISO Datum (YYYY-MM-DD). Der Code ist bis Ende dieses Tages gültig (Europe/Berlin).
 * appointmentDate: Termin im Schlaflabor
 * Wenn null/leer, wird ein Default (24h) verwendet.
 */
public class GenerateCodeRequest {

    @Size(max = 32)
    public String validUntilDate;

    @Size(max = 32)
    public String appointmentDate;
}
