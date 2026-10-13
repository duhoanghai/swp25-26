package de.schlaftagebuch.fhir;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import de.schlaftagebuch.report.dto.SleepReportResponse;
import de.schlaftagebuch.fhir.store.DiagnosticReportStore;
import de.schlaftagebuch.fhir.store.PatientStore;
import de.schlaftagebuch.report.model.ReportEntity;
import de.schlaftagebuch.report.repository.ReportRepository;
import org.hl7.fhir.r4.model.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.ZoneId;
import java.util.Date;
import java.util.UUID;

/**
 * Service für die Integration von Berichten mit FHIR DiagnosticReport.
 * Erstellt FHIR-konforme DiagnosticReports und speichert sie sowohl
 * in der PostgreSQL-Datenbank als auch im FHIR-Store.
 */
@Service
public class FhirReportService {

    private final DiagnosticReportStore diagnosticReportStore;
    private final PatientStore patientStore;
    private final ReportRepository reportRepository;
    private final FhirContext fhirContext;

    public FhirReportService(
            DiagnosticReportStore diagnosticReportStore,
            PatientStore patientStore,
            ReportRepository reportRepository,
            FhirContext fhirContext
    ) {
        this.diagnosticReportStore = diagnosticReportStore;
        this.patientStore = patientStore;
        this.reportRepository = reportRepository;
        this.fhirContext = fhirContext;
    }

    /**
     * Erstellt einen FHIR DiagnosticReport aus einem SleepReportResponse
     * und speichert ihn sowohl in FHIR als auch in der Datenbank.
     */
    @Transactional
    public ReportEntity createAndStoreDiagnosticReport(SleepReportResponse sleepReport, String reportType) {
        // Idempotenz: wenn exakt derselbe Zeitraum + Typ schon gespeichert ist,
        // erzeugen wir NICHT jedes Mal einen neuen DB-Eintrag.
        // (Sonst wächst die Report-Historie bei jedem GET /api/reports/me/pdf.)
        var existing = reportRepository
                .findFirstBySubjectRefAndReportTypeAndFromDateAndToDateOrderByCreatedAtDesc(
                        sleepReport.subjectRef,
                        reportType,
                        sleepReport.from,
                        sleepReport.to
                );

        if (existing.isPresent()) {
            // Trotzdem: DiagnosticReport ist im In-Memory Store evtl. nach Neustart leer.
            // Wir lassen das DB-JSON als Quelle und füllen den Store on-demand.
            try {
                var parser = fhirContext.newJsonParser();
                var dr = (DiagnosticReport) parser.parseResource(existing.get().getReportDataJson());
                String idPart = existing.get().getFhirDiagnosticReportRef();
                if (idPart != null && idPart.startsWith("DiagnosticReport/")) {
                    diagnosticReportStore.save(idPart.substring("DiagnosticReport/".length()), dr);
                }
            } catch (Exception ignored) {
                // Wenn das JSON nicht parsebar ist, ignorieren wir es hier.
            }

            return existing.get();
        }

        // 1. FHIR DiagnosticReport erstellen
        String reportId = UUID.randomUUID().toString();
        DiagnosticReport diagnosticReport = buildDiagnosticReport(reportId, sleepReport, reportType);

        // 2. Im FHIR Store speichern
        diagnosticReportStore.save(reportId, diagnosticReport);

        // 3. In PostgreSQL speichern
        ReportEntity entity = new ReportEntity();
        entity.setSubjectRef(sleepReport.subjectRef);
        entity.setReportType(reportType);
        entity.setFromDate(sleepReport.from);
        entity.setToDate(sleepReport.to);
        entity.setCreatedAt(Instant.now());
        entity.setDaysAvailable(sleepReport.daysAvailable);
        entity.setPatientName(sleepReport.patientName);

        if (sleepReport.summary != null) {
            entity.setAvgSleepMinutes(sleepReport.summary.avgSleepMinutes);
            entity.setAvgSleepQuality(sleepReport.summary.avgSleepQuality);
            entity.setAvgMorningFeeling(sleepReport.summary.avgMorningFeeling);
        }

        // JSON-Daten des vollständigen Berichts speichern
        IParser jsonParser = fhirContext.newJsonParser();
        entity.setReportDataJson(jsonParser.encodeResourceToString(diagnosticReport));
        entity.setFhirDiagnosticReportRef("DiagnosticReport/" + reportId);

        return reportRepository.save(entity);
    }

    public String getPatientDisplayName(String subjectRef) {
        if (subjectRef == null) return null;
        if (!subjectRef.startsWith("Patient/")) return subjectRef;

        String patientId = subjectRef.substring("Patient/".length());
        Patient p = patientStore.findById(patientId).orElse(null);
        if (p == null || !p.hasName() || p.getNameFirstRep() == null) return subjectRef;

        String given = p.getNameFirstRep().getGivenAsSingleString();
        String family = p.getNameFirstRep().getFamily();

        String full = (given == null ? "" : given.trim()) + " " + (family == null ? "" : family.trim());
        full = full.trim();

        return full.isBlank() ? subjectRef : full;
    }

    /**
     * Baut einen FHIR DiagnosticReport aus den Schlaf-Daten.
     */
    private DiagnosticReport buildDiagnosticReport(
            String reportId,
            SleepReportResponse sleepReport,
            String reportType
    ) {
        DiagnosticReport report = new DiagnosticReport();
        report.setId(reportId);

        // Status
        report.setStatus(DiagnosticReport.DiagnosticReportStatus.FINAL);

        // Code: Schlaftagebuch-Bericht
        CodeableConcept code = new CodeableConcept();
        code.setText("Sleep Diary Report");
        code.addCoding()
                .setSystem("http://loinc.org")
                .setCode("57852-6")
                .setDisplay("Sleep study");
        report.setCode(code);

        // Patient-Referenz
        report.setSubject(new Reference(sleepReport.subjectRef));

        // Zeitraum
        Period effectivePeriod = new Period();
        effectivePeriod.setStart(Date.from(
                sleepReport.from.atStartOfDay(ZoneId.systemDefault()).toInstant()
        ));
        effectivePeriod.setEnd(Date.from(
                sleepReport.to.atStartOfDay(ZoneId.systemDefault()).toInstant()
        ));
        report.setEffective(effectivePeriod);

        // Erstellungszeitpunkt
        report.setIssued(Date.from(Instant.now()));

        // Conclusion: Zusammenfassung
        if (sleepReport.summary != null) {
            StringBuilder conclusion = new StringBuilder();
            conclusion.append("Sleep Report Summary (").append(reportType).append("):\n");
            conclusion.append("Period: ").append(sleepReport.from).append(" to ").append(sleepReport.to).append("\n");
            conclusion.append("Days with data: ").append(sleepReport.daysAvailable).append("/").append(sleepReport.expectedDays).append("\n\n");

            if (sleepReport.summary.avgSleepMinutes != null) {
                conclusion.append("Average sleep duration: ")
                        .append(String.format("%.1f minutes\n", sleepReport.summary.avgSleepMinutes));
            }
            if (sleepReport.summary.avgSleepQuality != null) {
                conclusion.append("Average sleep quality: ")
                        .append(String.format("%.1f/5\n", sleepReport.summary.avgSleepQuality));
            }
            if (sleepReport.summary.avgMorningFeeling != null) {
                conclusion.append("Average morning feeling: ")
                        .append(String.format("%.1f/5\n", sleepReport.summary.avgMorningFeeling));
            }

            report.setConclusion(conclusion.toString());
        }

        // Kategorie
        CodeableConcept category = new CodeableConcept();
        category.addCoding()
                .setSystem("http://terminology.hl7.org/CodeSystem/v2-0074")
                .setCode("LAB")
                .setDisplay("Laboratory");
        report.addCategory(category);

        return report;
    }
}
