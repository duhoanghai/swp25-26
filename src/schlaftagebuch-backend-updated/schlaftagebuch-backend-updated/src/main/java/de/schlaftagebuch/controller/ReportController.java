package de.schlaftagebuch.controller;

import de.schlaftagebuch.dto.report.SleepReportResponse;
import de.schlaftagebuch.repository_database.UserAccountRepository;
import de.schlaftagebuch.service.PdfReportService;
import de.schlaftagebuch.service.ReportService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * REST-Controller für Arzt-Webseite: Bericht anzeigen.
 */
@PreAuthorize("hasRole('ARZT')")
@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;
    private final PdfReportService pdfReportService;
    private final UserAccountRepository userRepo;

    public ReportController(
            ReportService reportService,
            PdfReportService pdfReportService,
            UserAccountRepository userRepo
    ) {
        this.reportService = reportService;
        this.pdfReportService = pdfReportService;
        this.userRepo = userRepo;
    }

    /**
     * Bericht für den eingeloggten User ("me").
     * Patient sieht seinen eigenen Bericht, Arzt könnte damit ggf. eigenen subjectRef haben (falls vorhanden).
     */
    @GetMapping("/me")
    public SleepReportResponse myReport(
            Authentication auth,
            @RequestParam(defaultValue = "14") int days
    ) throws Exception {
        String authName = auth.getName();

        // authName kann username oder subjectRef sein -> wir lösen auf subjectRef
        String subjectRef = resolveSubjectRef(authName);

        return reportService.createLastDaysReport(subjectRef, days);
    }

    /**
     * Bericht für bestimmten Patienten (nur Arzt).
     * patientId hier ist UUID ohne "Patient/".
     */
    @GetMapping("/patient/{patientId}")
    public SleepReportResponse reportForPatient(
            @PathVariable String patientId,
            @RequestParam(defaultValue = "14") int days
    ) throws Exception {
        return reportService.createLastDaysReport("Patient/" + patientId, days);
    }

    /**
     * PDF-Export der 14-Tage-Zusammenfassung für einen Patienten (nur Arzt).
     */
    @GetMapping("/patient/{patientId}/pdf")
    public ResponseEntity<byte[]> reportPdf(
            @PathVariable String patientId,
            @RequestParam(defaultValue = "14") int days
    ) throws Exception {

        var report = reportService.createLastDaysReport("Patient/" + patientId, days);
        byte[] pdf = pdfReportService.createSummaryPdf(report);

        return ResponseEntity.ok()
                .header("Content-Type", "application/pdf")
                .header(
                        "Content-Disposition",
                        "attachment; filename=\"sleep-report-" + patientId + ".pdf\""
                )
                .body(pdf);
    }

    /**
     * Hilfsmethode: authName kann username oder bereits "Patient/..".
     * Wir machen es robust ähnlich wie dein ProtocolService.
     */
    private String resolveSubjectRef(String authName) {
        if (authName != null &&
                (authName.startsWith("Patient/") || authName.startsWith("Practitioner/"))) {
            return authName; // already a subjectRef
        }

        // sonst: username -> subjectRef in DB nachschlagen
        var user = userRepo.findByUsername(authName)
                .orElseThrow(() -> new IllegalArgumentException("Unknown user: " + authName));

        return user.getSubjectRef();
    }
}
