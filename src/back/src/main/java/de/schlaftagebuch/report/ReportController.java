package de.schlaftagebuch.report;

import de.schlaftagebuch.report.dto.SleepReportResponse;
import de.schlaftagebuch.report.dto.ReportHistoryResponse;
import de.schlaftagebuch.auth.repository.PatientRepository;
import de.schlaftagebuch.report.repository.ReportRepository;
import de.schlaftagebuch.auth.repository.UserAccountRepository;
import de.schlaftagebuch.fhir.FhirReportService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * Report-Endpunkte:
 * - /api/reports/me           -> JSON
 * - /api/reports/me/pdf       -> PDF
 *
 * NEU (Testmodus):
 * - allSubmissions=true  -> nimmt ALLE Submissions im Zeitraum (auch mehrere am selben Tag)
 * - allSubmissions=false -> Standard: pro Tag nur die neueste Submission
 */
@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;
    private final PdfReportService pdfReportService;
    private final ReportRepository reportRepo;
    private final UserAccountRepository userRepo;
    private final FhirReportService fhirReportService;
    private final PatientRepository patientRepo;

    public ReportController(
            ReportService reportService,
            PdfReportService pdfReportService,
            ReportRepository reportRepo,
            UserAccountRepository userRepo,
            FhirReportService fhirReportService,
            PatientRepository patientRepo
    ) {
        this.reportService = reportService;
        this.pdfReportService = pdfReportService;
        this.reportRepo = reportRepo;
        this.userRepo = userRepo;
        this.fhirReportService = fhirReportService;
        this.patientRepo = patientRepo;
    }

    private String resolvePatientName(String subjectRef) {
        String id = subjectRef.startsWith("Patient/") ? subjectRef.substring("Patient/".length()) : subjectRef;
        return patientRepo.findByPatientId(id)
                .map(p -> {
                    String given = p.getGivenName();
                    String family = p.getFamilyName();
                    if (family != null && !family.isBlank() && given != null && !given.isBlank()) return family + " " + given;
                    if (given != null && !given.isBlank()) return given;
                    if (family != null && !family.isBlank()) return family;
                    return subjectRef;
                })
                .orElse(subjectRef);
    }

    /**
     * Endbericht JSON für eingeloggten Patienten
     */
    @GetMapping("/me")
    public SleepReportResponse myReport(
            Authentication auth,
            @RequestParam(required = false) Integer days,
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to,
            @RequestParam(required = false, defaultValue = "false") boolean allSubmissions // <-- NEU
    ) throws Exception {

        String subjectRef = resolveSubjectRef(auth.getName());

        SleepReportResponse report;
        if (from != null && to != null) {
            report = reportService.createReportBetween(subjectRef, from, to, allSubmissions);
        } else {
            int d = (days == null ? 14 : days);
            report = reportService.createLastDaysReport(subjectRef, d, allSubmissions);
        }

        report.patientName = resolvePatientName(subjectRef);

        return report;
    }

    /**
     * Endbericht PDF für eingeloggten Patienten
     */
    @GetMapping("/me/pdf")
    public ResponseEntity<byte[]> myReportPdf(
            Authentication auth,
            @RequestParam(required = false) Integer days,
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to,
            @RequestParam(required = false, defaultValue = "false") boolean allSubmissions // <-- NEU
    ) throws Exception {

        String subjectRef = resolveSubjectRef(auth.getName());

        SleepReportResponse report;
        if (from != null && to != null) {
            report = reportService.createReportBetween(subjectRef, from, to, allSubmissions);
        } else {
            int d = (days == null ? 14 : days);
            report = reportService.createLastDaysReport(subjectRef, d, allSubmissions);
        }

        report.patientName = resolvePatientName(subjectRef);

        byte[] pdf = pdfReportService.createSummaryPdf(report);

        return ResponseEntity.ok()
                .header("Content-Type", "application/pdf")
                .header("Content-Disposition", "attachment; filename=\"sleep-report.pdf\"")
                .body(pdf);
    }

    /**
     * Arzt: JSON-Report für Patient
     */
    @GetMapping("/patient/{patientId}")
    public SleepReportResponse reportForPatient(
            @PathVariable String patientId,
            @RequestParam(required = false) Integer days,
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to,
            @RequestParam(required = false, defaultValue = "false") boolean allSubmissions // <-- NEU
    ) throws Exception {

        String subjectRef = "Patient/" + patientId;

        SleepReportResponse report;
        if (from != null && to != null) {
            report = reportService.createReportBetween(subjectRef, from, to, allSubmissions);
        } else {
            int d = (days == null ? 14 : days);
            report = reportService.createLastDaysReport(subjectRef, d, allSubmissions);
        }

        report.patientName = resolvePatientName(subjectRef);

        return report;
    }

    /**
     * Arzt: Endbericht PDF für Patient
     */
    @GetMapping("/patient/{patientId}/pdf")
    public ResponseEntity<byte[]> reportPdf(
            @PathVariable String patientId,
            @RequestParam(required = false) Integer days,
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to,
            @RequestParam(required = false, defaultValue = "false") boolean allSubmissions // <-- NEU
    ) throws Exception {

        String subjectRef = "Patient/" + patientId;

        SleepReportResponse report;
        if (from != null && to != null) {
            report = reportService.createReportBetween(subjectRef, from, to, allSubmissions);
        } else {
            int d = (days == null ? 14 : days);
            report = reportService.createLastDaysReport(subjectRef, d, allSubmissions);
        }

        report.patientName = resolvePatientName(subjectRef);

        byte[] pdf = pdfReportService.createSummaryPdf(report);

        return ResponseEntity.ok()
                .header("Content-Type", "application/pdf")
                .header("Content-Disposition",
                        "attachment; filename=\"sleep-report-" + patientId + ".pdf\"")
                .body(pdf);
    }

    /**
     * Liste der Reports (History) für eingeloggten Benutzer
     */
    @GetMapping("/me/history")
    public List<ReportHistoryResponse> myReportHistory(Authentication auth) {
        String subjectRef = resolveSubjectRef(auth.getName());

        return reportRepo.findAllBySubjectRefOrderByCreatedAtDesc(subjectRef)
                .stream()
                .map(ReportHistoryResponse::fromEntity)
                .toList();
    }

    /**
     * Arzt: Liste der generierten Reports (History) für einen Patienten
     */
    @GetMapping("/history/patient/{patientId}")
    public List<ReportHistoryResponse> patientReportHistory(@PathVariable String patientId) {
        String subjectRef = "Patient/" + patientId;
        return reportRepo.findAllBySubjectRefOrderByCreatedAtDesc(subjectRef)
                .stream()
                .map(ReportHistoryResponse::fromEntity)
                .toList();
    }


    private String resolveSubjectRef(String authName) {
        if (authName.startsWith("Patient/") || authName.startsWith("Practitioner/")) {
            return authName;
        }

        return userRepo.findByUsername(authName)
                .orElseThrow()
                .getSubjectRef();
    }

}
