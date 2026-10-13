package de.schlaftagebuch.report.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Datenbank-Entität für generierte Schlaf-Berichte.
 * Speichert Metadaten über erstellte Reports (JSON und PDF).
 */
@Entity
@Table(name = "sleep_report", indexes = {
        @Index(name = "idx_report_subject", columnList = "subject_ref"),
        @Index(name = "idx_report_date_range", columnList = "from_date, to_date")
})
public class ReportEntity {

    @Id
    @GeneratedValue
    private UUID id;

    /**
     * Referenz auf FHIR Patient (z.B. "Patient/{uuid}")
     */
    @Column(nullable = false, length = 120, name = "subject_ref")
    private String subjectRef;
    @Column(name = "patient_name", length = 200)
    private String patientName;
    /**
     * Typ des Berichts (z.B. "SUMMARY", "DAILY")
     */
    @Column(nullable = false, length = 50)
    private String reportType;

    /**
     * Startdatum des Berichtszeitraums (inklusiv)
     */
    @Column(nullable = false, name = "from_date")
    private LocalDate fromDate;

    /**
     * Enddatum des Berichtszeitraums (inklusiv)
     */
    @Column(nullable = false, name = "to_date")
    private LocalDate toDate;

    /**
     * Zeitpunkt der Report-Erstellung
     */
    @Column(nullable = false, name = "created_at")
    private Instant createdAt;

    /**
     * Anzahl der Tage mit Daten im Bericht
     */
    @Column(name = "days_available")
    private Integer daysAvailable;

    /**
     * Durchschnittliche Schlafdauer in Minuten
     */
    @Column(name = "avg_sleep_minutes")
    private Double avgSleepMinutes;

    /**
     * Durchschnittliche Schlafqualität (1-5)
     */
    @Column(name = "avg_sleep_quality")
    private Double avgSleepQuality;

    /**
     * Durchschnittliches Morgengefühl (1-5)
     */
    @Column(name = "avg_morning_feeling")
    private Double avgMorningFeeling;

    /**
     * JSON-Daten des vollständigen Berichts (komprimiert)
     */
    @Column(columnDefinition = "TEXT", name = "report_data_json")
    private String reportDataJson;

    /**
     * Optionale Referenz auf FHIR DiagnosticReport
     */
    @Column(length = 120, name = "fhir_diagnostic_report_ref")
    private String fhirDiagnosticReportRef;

    // === Getter & Setter ===

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getSubjectRef() {
        return subjectRef;
    }

    public void setSubjectRef(String subjectRef) {
        this.subjectRef = subjectRef;
    }

    public String getPatientName() {return patientName;}
    public void setPatientName(String patientName) { this.patientName = patientName;}

    public String getReportType() {
        return reportType;
    }

    public void setReportType(String reportType) {
        this.reportType = reportType;
    }

    public LocalDate getFromDate() {
        return fromDate;
    }

    public void setFromDate(LocalDate fromDate) {
        this.fromDate = fromDate;
    }

    public LocalDate getToDate() {
        return toDate;
    }

    public void setToDate(LocalDate toDate) {
        this.toDate = toDate;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Integer getDaysAvailable() {
        return daysAvailable;
    }

    public void setDaysAvailable(Integer daysAvailable) {
        this.daysAvailable = daysAvailable;
    }

    public Double getAvgSleepMinutes() {
        return avgSleepMinutes;
    }

    public void setAvgSleepMinutes(Double avgSleepMinutes) {
        this.avgSleepMinutes = avgSleepMinutes;
    }

    public Double getAvgSleepQuality() {
        return avgSleepQuality;
    }

    public void setAvgSleepQuality(Double avgSleepQuality) {
        this.avgSleepQuality = avgSleepQuality;
    }

    public Double getAvgMorningFeeling() {
        return avgMorningFeeling;
    }

    public void setAvgMorningFeeling(Double avgMorningFeeling) {
        this.avgMorningFeeling = avgMorningFeeling;
    }

    public String getReportDataJson() {
        return reportDataJson;
    }

    public void setReportDataJson(String reportDataJson) {
        this.reportDataJson = reportDataJson;
    }

    public String getFhirDiagnosticReportRef() {
        return fhirDiagnosticReportRef;
    }

    public void setFhirDiagnosticReportRef(String fhirDiagnosticReportRef) {
        this.fhirDiagnosticReportRef = fhirDiagnosticReportRef;
    }
}
