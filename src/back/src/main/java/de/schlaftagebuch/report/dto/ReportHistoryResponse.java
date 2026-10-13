package de.schlaftagebuch.report.dto;

import de.schlaftagebuch.report.model.ReportEntity;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Response DTO für die Report-Historie.
 * Zeigt eine Liste aller generierten Berichte für einen Patienten.
 */
public class ReportHistoryResponse {

    public UUID id;
    public String subjectRef;
    public String reportType;
    public LocalDate fromDate;
    public LocalDate toDate;
    public Instant createdAt;
    public Integer daysAvailable;
    public Double avgSleepMinutes;
    public Double avgSleepQuality;
    public Double avgMorningFeeling;
    public String fhirDiagnosticReportRef;

    /**
     * Konvertiert eine ReportEntity in ein DTO.
     */
    public static ReportHistoryResponse fromEntity(ReportEntity entity) {
        ReportHistoryResponse response = new ReportHistoryResponse();
        response.id = entity.getId();
        response.subjectRef = entity.getSubjectRef();
        response.reportType = entity.getReportType();
        response.fromDate = entity.getFromDate();
        response.toDate = entity.getToDate();
        response.createdAt = entity.getCreatedAt();
        response.daysAvailable = entity.getDaysAvailable();
        response.avgSleepMinutes = entity.getAvgSleepMinutes();
        response.avgSleepQuality = entity.getAvgSleepQuality();
        response.avgMorningFeeling = entity.getAvgMorningFeeling();
        response.fhirDiagnosticReportRef = entity.getFhirDiagnosticReportRef();
        return response;
    }
}