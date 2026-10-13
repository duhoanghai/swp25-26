package de.schlaftagebuch.arztManagement.dto;

import java.time.Instant;
import java.util.UUID;

/**
 * DTO zum Anzeigen/Verwalten von Einmal-Codes im Arzt-Frontend.
 */
public class CodeInfoResponse {
    public UUID id;
    public String code;
    public String patientRef;
    public Instant createdAt;
    public Instant expiresAt;
    public Instant usedAt;
    public Instant revokedAt;
    public String appointmentDate;

    public String status; // ACTIVE, USED, EXPIRED, REVOKED

    public CodeInfoResponse() {}
}
