package de.schlaftagebuch.arztManagement.dto;

import java.time.Instant;

/**
 * Zeile für die Berichte-Tabelle im Arzt-Frontend.
 */
public class PatientReportRowResponse {
    public String patientId;
    public String patientRef;
    public String display;

    public long reportsCount;
    public Instant latestReportCreatedAt;
    public String latestReportType;
}
