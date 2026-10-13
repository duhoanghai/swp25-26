package de.schlaftagebuch.arztManagement.dto;

import java.time.Instant;
import java.util.UUID;

/**
 * Detailinfos für die Patientenseite im Arzt-Frontend.
 * (Nur Daten, die wir sicher aus der DB ableiten können.)
 */
public class PatientDetailResponse {
    public String patientId;
    public String patientRef;
    public String username;
    public String display;
    public String givenName;
    public String familyName;
    public String birthDate;
    public long reportsCount;
    public Instant latestReportCreatedAt;

    public UUID latestCodeId;
    public String latestCode;
    public String latestCodeStatus; // ACTIVE/USED/EXPIRED/REVOKED
    public Instant latestCodeCreatedAt;
    public Instant latestCodeExpiresAt;
    public Instant latestCodeUsedAt;
    public Instant latestCodeRevokedAt;
    public String latestAppointmentDate;
}
