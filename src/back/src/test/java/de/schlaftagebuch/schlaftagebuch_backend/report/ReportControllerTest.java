package de.schlaftagebuch.schlaftagebuch_backend.report;


import de.schlaftagebuch.report.PdfReportService;
import de.schlaftagebuch.report.ReportController;
import de.schlaftagebuch.report.ReportService;
import de.schlaftagebuch.report.dto.SleepReportResponse;

import de.schlaftagebuch.auth.repository.PatientRepository;
import de.schlaftagebuch.report.repository.ReportRepository;
import de.schlaftagebuch.auth.repository.UserAccountRepository;
import de.schlaftagebuch.fhir.FhirReportService;
import de.schlaftagebuch.auth.dto.PatientEntity;
import de.schlaftagebuch.auth.model.UserAccountEntity;
import de.schlaftagebuch.auth.model.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.time.LocalDate;
import java.time.Instant;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportControllerTest {

    @Mock
    private ReportService reportService;

    @Mock
    private PdfReportService pdfReportService;

    @Mock
    private ReportRepository reportRepo;

    @Mock
    private UserAccountRepository userRepo;

    @Mock
    private FhirReportService fhirReportService;

    @Mock
    private PatientRepository patientRepo;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private ReportController reportController;

    private SleepReportResponse sleepReport;
    private UserAccountEntity patientAccount;
    private PatientEntity patient;

    @BeforeEach
    void setUp() {
        // Setup patient account
        patientAccount = new UserAccountEntity();
        patientAccount.setId(UUID.randomUUID());
        patientAccount.setUsername("PAT12345");
        patientAccount.setRole(Role.PATIENT);
        patientAccount.setSubjectRef("Patient/patient-123");

        // Setup patient entity
        patient = new PatientEntity();
        patient.setPatientId("patient-123");
        patient.setGivenName("Max");
        patient.setFamilyName("Mustermann");

        // Setup sleep report
        sleepReport = new SleepReportResponse();
        sleepReport.subjectRef = "Patient/patient-123";
        sleepReport.patientName = "Max Mustermann";
        sleepReport.from = LocalDate.parse("2024-01-01");
        sleepReport.to = LocalDate.parse("2024-01-14");
        sleepReport.expectedDays = 14;
        sleepReport.daysAvailable = 10;
        sleepReport.days = new ArrayList<>();
        sleepReport.summary = new SleepReportResponse.Summary();
        sleepReport.summary.avgSleepMinutes = 480.0;
        sleepReport.summary.avgSleepQuality = 4.2;
        sleepReport.summary.avgMorningFeeling = 3.8;
    }

    // ============ myReport Tests ============
    @Test
    void myReport_withSubjectRefAuth_shouldReturnReport() throws Exception {
        // Arrange
        String subjectRef = "Patient/patient-123";
        when(authentication.getName()).thenReturn(subjectRef);

        when(reportService.createLastDaysReport(subjectRef, 14, false))
                .thenReturn(sleepReport);

        when(patientRepo.findByPatientId("patient-123"))
                .thenReturn(Optional.of(patient));

        // Act
        SleepReportResponse response = reportController.myReport(
                authentication, null, null, null, false);

        // Assert
        assertNotNull(response);
        assertEquals("Patient/patient-123", response.subjectRef);
       // assertEquals("Max Mustermann", response.patientName);

        verify(authentication).getName();
        verify(reportService).createLastDaysReport(subjectRef, 14, false);
        verify(patientRepo).findByPatientId("patient-123");
    }

    @Test
    void myReport_withUsernameAuth_shouldResolveSubjectRef() throws Exception {
        // Arrange
        String username = "PAT12345";
        String subjectRef = "Patient/patient-123";

        when(authentication.getName()).thenReturn(username);
        when(userRepo.findByUsername(username)).thenReturn(Optional.of(patientAccount));

        when(reportService.createLastDaysReport(subjectRef, 14, false))
                .thenReturn(sleepReport);

        when(patientRepo.findByPatientId("patient-123"))
                .thenReturn(Optional.of(patient));

        // Act
        SleepReportResponse response = reportController.myReport(
                authentication, null, null, null, false);

        // Assert
        assertNotNull(response);
        verify(authentication).getName();
        verify(userRepo).findByUsername(username);
        verify(reportService).createLastDaysReport(subjectRef, 14, false);
    }

    @Test
    void myReport_withCustomDays_shouldPassDaysParameter() throws Exception {
        // Arrange
        String subjectRef = "Patient/patient-123";
        int customDays = 30;

        when(authentication.getName()).thenReturn(subjectRef);

        when(reportService.createLastDaysReport(subjectRef, customDays, false))
                .thenReturn(sleepReport);

        when(patientRepo.findByPatientId("patient-123"))
                .thenReturn(Optional.of(patient));

        // Act
        SleepReportResponse response = reportController.myReport(
                authentication, customDays, null, null, false);

        // Assert
        assertNotNull(response);
        verify(reportService).createLastDaysReport(subjectRef, customDays, false);
    }

    @Test
    void myReport_withDateRange_shouldCallCreateReportBetween() throws Exception {
        // Arrange
        String subjectRef = "Patient/patient-123";
        LocalDate from = LocalDate.parse("2024-01-01");
        LocalDate to = LocalDate.parse("2024-01-31");

        when(authentication.getName()).thenReturn(subjectRef);

        when(reportService.createReportBetween(subjectRef, from, to, false))
                .thenReturn(sleepReport);

        when(patientRepo.findByPatientId("patient-123"))
                .thenReturn(Optional.of(patient));

        // Act
        SleepReportResponse response = reportController.myReport(
                authentication, null, from, to, false);

        // Assert
        assertNotNull(response);
        verify(reportService).createReportBetween(subjectRef, from, to, false);
    }

    @Test
    void myReport_withAllSubmissionsTrue_shouldPassParameter() throws Exception {
        // Arrange
        String subjectRef = "Patient/patient-123";
        boolean allSubmissions = true;

        when(authentication.getName()).thenReturn(subjectRef);

        when(reportService.createLastDaysReport(subjectRef, 14, allSubmissions))
                .thenReturn(sleepReport);

        when(patientRepo.findByPatientId("patient-123"))
                .thenReturn(Optional.of(patient));

        // Act
        SleepReportResponse response = reportController.myReport(
                authentication, null, null, null, allSubmissions);

        // Assert
        assertNotNull(response);
        verify(reportService).createLastDaysReport(subjectRef, 14, allSubmissions);
    }

    // ============ myReportPdf Tests ============
    @Test
    void myReportPdf_shouldReturnPdfResponse() throws Exception {
        // Arrange
        String subjectRef = "Patient/patient-123";
        byte[] pdfBytes = "PDF_CONTENT".getBytes();

        when(authentication.getName()).thenReturn(subjectRef);

        when(reportService.createLastDaysReport(subjectRef, 14, false))
                .thenReturn(sleepReport);

        when(pdfReportService.createSummaryPdf(sleepReport))
                .thenReturn(pdfBytes);

        when(patientRepo.findByPatientId("patient-123"))
                .thenReturn(Optional.of(patient));

        // Act
        ResponseEntity<byte[]> response = reportController.myReportPdf(
                authentication, null, null, null, false);

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertEquals("application/pdf", response.getHeaders().getFirst("Content-Type"));
        assertEquals("attachment; filename=\"sleep-report.pdf\"",
                response.getHeaders().getFirst("Content-Disposition"));
        assertArrayEquals(pdfBytes, response.getBody());

        verify(pdfReportService).createSummaryPdf(sleepReport);
    }

    // ============ reportForPatient Tests (Arzt Endpunkt) ============
    @Test
    void reportForPatient_shouldReturnPatientReport() throws Exception {
        // Arrange
        String patientId = "patient-123";
        String subjectRef = "Patient/" + patientId;

        when(reportService.createLastDaysReport(subjectRef, 14, false))
                .thenReturn(sleepReport);

        when(patientRepo.findByPatientId(patientId))
                .thenReturn(Optional.of(patient));

        // Act
        SleepReportResponse response = reportController.reportForPatient(
                patientId, null, null, null, false);

        // Assert
        assertNotNull(response);
        assertEquals(subjectRef, response.subjectRef);
       // assertEquals("Max Mustermann", response.patientName);

        verify(reportService).createLastDaysReport(subjectRef, 14, false);
        verify(patientRepo).findByPatientId(patientId);
    }

    @Test
    void reportForPatient_withDateRange_shouldCallCreateReportBetween() throws Exception {
        // Arrange
        String patientId = "patient-123";
        String subjectRef = "Patient/" + patientId;
        LocalDate from = LocalDate.parse("2024-01-01");
        LocalDate to = LocalDate.parse("2024-01-31");

        when(reportService.createReportBetween(subjectRef, from, to, false))
                .thenReturn(sleepReport);

        when(patientRepo.findByPatientId(patientId))
                .thenReturn(Optional.of(patient));

        // Act
        SleepReportResponse response = reportController.reportForPatient(
                patientId, null, from, to, false);

        // Assert
        assertNotNull(response);
        verify(reportService).createReportBetween(subjectRef, from, to, false);
    }

    // ============ reportPdf Tests (Arzt Endpunkt) ============
    @Test
    void reportPdf_shouldReturnPdfForPatient() throws Exception {
        // Arrange
        String patientId = "patient-123";
        String subjectRef = "Patient/" + patientId;
        byte[] pdfBytes = "PDF_CONTENT".getBytes();

        when(reportService.createLastDaysReport(subjectRef, 14, false))
                .thenReturn(sleepReport);

        when(pdfReportService.createSummaryPdf(sleepReport))
                .thenReturn(pdfBytes);

        when(patientRepo.findByPatientId(patientId))
                .thenReturn(Optional.of(patient));

        // Act
        ResponseEntity<byte[]> response = reportController.reportPdf(
                patientId, null, null, null, false);

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertEquals("application/pdf", response.getHeaders().getFirst("Content-Type"));
        assertTrue(response.getHeaders().getFirst("Content-Disposition")
                .contains("sleep-report-patient-123.pdf"));
        assertArrayEquals(pdfBytes, response.getBody());

        verify(pdfReportService).createSummaryPdf(sleepReport);
    }
}