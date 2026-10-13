package de.schlaftagebuch.schlaftagebuch_backend.report;
import de.schlaftagebuch.report.ReportService;
import de.schlaftagebuch.report.dto.SleepReportResponse;
import de.schlaftagebuch.protocol.model.ProtocolSubmissionEntity;
import de.schlaftagebuch.protocol.repository.ProtocolSubmissionRepository;
import de.schlaftagebuch.fhir.FhirReportService;
import de.schlaftagebuch.auth.dto.PatientEntity;
import de.schlaftagebuch.auth.repository.PatientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.time.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
//Auskommentiert Test enthalten private Methoden, Diese Tests haben bestanden wenn die entsprechende Methoden Public eingesetzt.
@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock
    private ProtocolSubmissionRepository submissionRepo;

    @Mock
    private FhirReportService fhirReportService;

    @Mock
    private PatientRepository patientRepo;

    @InjectMocks
    private ReportService reportService;

    private ProtocolSubmissionEntity submission1;
    private ProtocolSubmissionEntity submission2;
    private PatientEntity patient;

    @Mock
    private Clock clock;

    @BeforeEach
    void setUp() throws Exception {
        // Setup test patient
        patient = new PatientEntity();
        patient.setPatientId("patient-123");
        patient.setGivenName("Max");
        patient.setFamilyName("Mustermann");

        // Setup first submission (day 1)
        submission1 = new ProtocolSubmissionEntity();
        submission1.setId(UUID.randomUUID());
        submission1.setSubjectRef("Patient/patient-123");
        submission1.setTemplateKey("sleep_protocol_v1");
        submission1.setLocale("de-DE");
        submission1.setFilledAt(Instant.parse("2024-01-15T22:30:00Z"));
        submission1.setCreatedAt(Instant.parse("2024-01-15T22:35:00Z"));

        String answersJson1 = "["
                + "{\"id\":0,\"result\":\"3.0\"},"
                + "{\"id\":1,\"result\":\"Ja\"},"
                + "{\"id\":2,\"result\":\"22:30\"},"
                + "{\"id\":3,\"result\":\"07:15\"},"
                + "{\"id\":4,\"result\":\"4.0\"},"
                + "{\"id\":5,\"result\":\"3.0\"},"
                + "{\"id\":6,\"result\":\"Nein\"},"
                + "{\"id\":7,\"result\":\"0\"},"
                + "{\"id\":8,\"result\":\"00:00\"},"
                + "{\"id\":9,\"result\":\"Lesen\"},"
                + "{\"id\":10,\"result\":\"Ja\"},"
                + "{\"id\":11,\"result\":\"2\"},"
                + "{\"id\":12,\"result\":\"00:20\"},"
                + "{\"id\":13,\"result\":\"Nein\"},"
                + "{\"id\":14,\"result\":\"\"},"
                + "{\"id\":15,\"result\":\"Ja\"},"
                + "{\"id\":16,\"result\":\"01:30\"}"
                + "]";
        submission1.setAnswersJson(answersJson1);

        // Setup second submission (day 2)
        submission2 = new ProtocolSubmissionEntity();
        submission2.setId(UUID.randomUUID());
        submission2.setSubjectRef("Patient/patient-123");
        submission2.setTemplateKey("sleep_protocol_v1");
        submission2.setLocale("de-DE");
        submission2.setFilledAt(Instant.parse("2024-01-16T22:45:00Z"));
        submission2.setCreatedAt(Instant.parse("2024-01-16T22:50:00Z"));

        String answersJson2 = "["
                + "{\"id\":0,\"result\":\"4.0\"},"
                + "{\"id\":1,\"result\":\"Nein\"},"
                + "{\"id\":2,\"result\":\"23:00\"},"
                + "{\"id\":3,\"result\":\"06:30\"},"
                + "{\"id\":4,\"result\":\"5.0\"},"
                + "{\"id\":5,\"result\":\"4.0\"},"
                + "{\"id\":6,\"result\":\"Ja\"},"
                + "{\"id\":7,\"result\":\"1\"},"
                + "{\"id\":8,\"result\":\"00:30\"},"
                + "{\"id\":9,\"result\":\"Meditieren\"},"
                + "{\"id\":10,\"result\":\"Nein\"},"
                + "{\"id\":11,\"result\":\"0\"},"
                + "{\"id\":12,\"result\":\"00:00\"},"
                + "{\"id\":13,\"result\":\"Ja\"},"
                + "{\"id\":14,\"result\":\"Abends\"},"
                + "{\"id\":15,\"result\":\"Nein\"},"
                + "{\"id\":16,\"result\":\"00:45\"}"
                + "]";
        submission2.setAnswersJson(answersJson2);


    }

    // ============ createLastDaysReport Tests ============
    @Test
    void createLastDaysReport_withValidData_shouldReturnReport() throws Exception {
        // Arrange
        String subjectRef = "Patient/patient-123";
        int days = 14;
        LocalDate to = LocalDate.now();
        LocalDate from = to.minusDays(days - 1L);

        Instant fromInstant = from.atStartOfDay(ZoneId.systemDefault()).toInstant();
        Instant toInstant = to.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant();

        List<ProtocolSubmissionEntity> submissions = Arrays.asList(submission1, submission2);

        when(submissionRepo.findAllBySubjectRefAndFilledAtGreaterThanEqualAndFilledAtLessThanOrderByFilledAtAsc(
                eq(subjectRef), eq(fromInstant), eq(toInstant)))
                .thenReturn(submissions);

        when(patientRepo.findByPatientId("patient-123"))
                .thenReturn(Optional.of(patient));

        // FIX: Use thenReturn() instead of doNothing() for non-void method
        when(fhirReportService.createAndStoreDiagnosticReport(any(SleepReportResponse.class), anyString()))
                .thenReturn(null); // or appropriate return value

        // Act
        SleepReportResponse response = reportService.createLastDaysReport(subjectRef, days, false);

        // Assert
        assertNotNull(response);
        assertEquals(subjectRef, response.subjectRef);
        assertEquals(from, response.from);
        assertEquals(to, response.to);
        assertEquals(days, response.expectedDays);
        assertEquals(2, response.daysAvailable);
        assertEquals("Mustermann Max", response.patientName);

        assertNotNull(response.days);
        assertEquals(2, response.days.size());

        assertNotNull(response.summary);
        assertNotNull(response.summary.avgSleepMinutes);
        assertNotNull(response.summary.avgSleepQuality);
        assertNotNull(response.summary.avgMorningFeeling);

        verify(submissionRepo).findAllBySubjectRefAndFilledAtGreaterThanEqualAndFilledAtLessThanOrderByFilledAtAsc(
                eq(subjectRef), eq(fromInstant), eq(toInstant));
        verify(fhirReportService).createAndStoreDiagnosticReport(any(SleepReportResponse.class), eq("LAST_DAYS"));
    }

    @Test
    void createLastDaysReport_withNegativeDays_shouldThrowException() {
        // Arrange
        String subjectRef = "Patient/patient-123";
        int days = -5;

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> reportService.createLastDaysReport(subjectRef, days, false)
        );
        assertEquals("days must be > 0", exception.getMessage());
    }

    @Test
    void createLastDaysReport_withZeroDays_shouldThrowException() {
        // Arrange
        String subjectRef = "Patient/patient-123";
        int days = 0;

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> reportService.createLastDaysReport(subjectRef, days, false)
        );
        assertEquals("days must be > 0", exception.getMessage());
    }

//    @Test
//    void createLastDaysReport_withAllSubmissionsTrue_shouldIncludeAll() throws Exception {
//        // Arrange
//        String subjectRef = "Patient/patient-123";
//        int days = 1;
//
//        ProtocolSubmissionEntity sameDaySubmission1 = createSubmissionForDate("2024-01-15T10:00:00Z");
//        ProtocolSubmissionEntity sameDaySubmission2 = createSubmissionForDate("2024-01-15T22:00:00Z");
//
//        List<ProtocolSubmissionEntity> submissions = Arrays.asList(
//                sameDaySubmission1, sameDaySubmission2
//        );
//
//        // Verwende any() für die Instant-Parameter statt eq()
//        when(submissionRepo.findAllBySubjectRefAndFilledAtGreaterThanEqualAndFilledAtLessThanOrderByFilledAtAsc(
//                eq(subjectRef),
//                any(Instant.class),   // Akzeptiere JEDEN Instant-Wert
//                any(Instant.class)))   // Akzeptiere JEDEN Instant-Wert
//                .thenReturn(submissions);
//
//        when(patientRepo.findByPatientId("patient-123"))
//                .thenReturn(Optional.of(patient));
//
//        when(fhirReportService.createAndStoreDiagnosticReport(any(SleepReportResponse.class), anyString()))
//                .thenReturn(null);
//
//        // Act
//        SleepReportResponse response = reportService.createLastDaysReport(subjectRef, days, true);
//
//        // Assert
//        assertNotNull(response);
//        assertEquals(2, response.daysAvailable);
//        assertEquals(2, response.days.size());
//    }
//    @Test
//    void createLastDaysReport_withAllSubmissionsFalse_shouldTakeLatestPerDay() throws Exception {
//        // Arrange
//        String subjectRef = "Patient/patient-123";
//        int days = 1;
//
//        ProtocolSubmissionEntity earlierSubmission = createSubmissionForDate("2024-01-15T10:00:00Z");
//        ProtocolSubmissionEntity laterSubmission = createSubmissionForDate("2024-01-15T22:00:00Z");
//
//        List<ProtocolSubmissionEntity> submissions = Arrays.asList(
//                earlierSubmission, laterSubmission
//        );
//
//        // ArgumentCaptor für die Instant-Parameter
//        ArgumentCaptor<Instant> fromCaptor = ArgumentCaptor.forClass(Instant.class);
//        ArgumentCaptor<Instant> toCaptor = ArgumentCaptor.forClass(Instant.class);
//
//        when(submissionRepo.findAllBySubjectRefAndFilledAtGreaterThanEqualAndFilledAtLessThanOrderByFilledAtAsc(
//                eq(subjectRef),
//                fromCaptor.capture(),
//                toCaptor.capture()))
//                .thenReturn(submissions);
//
//        when(patientRepo.findByPatientId("patient-123"))
//                .thenReturn(Optional.of(patient));
//
//        when(fhirReportService.createAndStoreDiagnosticReport(any(SleepReportResponse.class), anyString()))
//                .thenReturn(null);
//
//        // Act
//        SleepReportResponse response = reportService.createLastDaysReport(subjectRef, days, false);
//
//        // Assert
//        assertNotNull(response);
//        assertEquals(1, response.daysAvailable);
//
//        // Optional: Überprüfe die übergebenen Parameter
//        Instant capturedFrom = fromCaptor.getValue();
//        Instant capturedTo = toCaptor.getValue();
//
//        // Du kannst jetzt assertions auf capturedFrom und capturedTo machen
//        assertNotNull(capturedFrom);
//        assertNotNull(capturedTo);
//        assertTrue(capturedTo.isAfter(capturedFrom));
//    }
    @Test
    void createLastDaysReport_withNoSubmissions_shouldReturnEmptyReport() throws Exception {
        // Arrange
        String subjectRef = "Patient/patient-123";
        int days = 14;

        Instant fromInstant = LocalDate.now().minusDays(13).atStartOfDay(ZoneId.systemDefault()).toInstant();
        Instant toInstant = LocalDate.now().plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant();

        when(submissionRepo.findAllBySubjectRefAndFilledAtGreaterThanEqualAndFilledAtLessThanOrderByFilledAtAsc(
                eq(subjectRef), eq(fromInstant), eq(toInstant)))
                .thenReturn(Collections.emptyList());

        when(patientRepo.findByPatientId("patient-123"))
                .thenReturn(Optional.of(patient));

        // FIX: Use thenReturn() instead of doNothing()
        when(fhirReportService.createAndStoreDiagnosticReport(any(SleepReportResponse.class), anyString()))
                .thenReturn(null);

        // Act
        SleepReportResponse response = reportService.createLastDaysReport(subjectRef, days, false);

        // Assert
        assertNotNull(response);
        assertEquals(0, response.daysAvailable);
        assertTrue(response.days.isEmpty());
        assertNotNull(response.summary);
        assertEquals("Mustermann Max", response.patientName);

        verify(submissionRepo).findAllBySubjectRefAndFilledAtGreaterThanEqualAndFilledAtLessThanOrderByFilledAtAsc(
                eq(subjectRef), eq(fromInstant), eq(toInstant));
    }

    // ============ createReportBetween Tests ============
    @Test
    void createReportBetween_withValidRange_shouldReturnReport() throws Exception {
        // Arrange
        String subjectRef = "Patient/patient-123";
        LocalDate from = LocalDate.parse("2024-01-01");
        LocalDate to = LocalDate.parse("2024-01-14");

        Instant fromInstant = from.atStartOfDay(ZoneId.systemDefault()).toInstant();
        Instant toInstant = to.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant();

        List<ProtocolSubmissionEntity> submissions = Arrays.asList(submission1, submission2);

        when(submissionRepo.findAllBySubjectRefAndFilledAtGreaterThanEqualAndFilledAtLessThanOrderByFilledAtAsc(
                eq(subjectRef), eq(fromInstant), eq(toInstant)))
                .thenReturn(submissions);

        when(patientRepo.findByPatientId("patient-123"))
                .thenReturn(Optional.of(patient));

        // FIX: Use thenReturn() instead of doNothing()
        when(fhirReportService.createAndStoreDiagnosticReport(any(SleepReportResponse.class), anyString()))
                .thenReturn(null);

        // Act
        SleepReportResponse response = reportService.createReportBetween(subjectRef, from, to, false);

        // Assert
        assertNotNull(response);
        assertEquals(subjectRef, response.subjectRef);
        assertEquals(from, response.from);
        assertEquals(to, response.to);
        assertEquals(14, response.expectedDays); // 14 days in range
        assertEquals(2, response.daysAvailable);

        verify(submissionRepo).findAllBySubjectRefAndFilledAtGreaterThanEqualAndFilledAtLessThanOrderByFilledAtAsc(
                eq(subjectRef), eq(fromInstant), eq(toInstant));
        verify(fhirReportService).createAndStoreDiagnosticReport(any(SleepReportResponse.class), eq("RANGE"));
    }

    @Test
    void createReportBetween_withSwappedDates_shouldSwapAndReturnReport() throws Exception {
        // Arrange
        String subjectRef = "Patient/patient-123";
        LocalDate laterDate = LocalDate.parse("2024-01-14");
        LocalDate earlierDate = LocalDate.parse("2024-01-01");

        // Pass dates in wrong order
        when(submissionRepo.findAllBySubjectRefAndFilledAtGreaterThanEqualAndFilledAtLessThanOrderByFilledAtAsc(
                anyString(), any(), any()))
                .thenReturn(Collections.emptyList());

        when(patientRepo.findByPatientId("patient-123"))
                .thenReturn(Optional.of(patient));

        // FIX: Use thenReturn() instead of doNothing()
        when(fhirReportService.createAndStoreDiagnosticReport(any(SleepReportResponse.class), anyString()))
                .thenReturn(null);

        // Act
        SleepReportResponse response = reportService.createReportBetween(
                subjectRef, laterDate, earlierDate, false);

        // Assert
        assertNotNull(response);
        assertEquals(earlierDate, response.from); // Should be swapped
        assertEquals(laterDate, response.to);     // Should be swapped
        assertEquals(14, response.expectedDays);  // 14 days in range

        verify(submissionRepo).findAllBySubjectRefAndFilledAtGreaterThanEqualAndFilledAtLessThanOrderByFilledAtAsc(
                anyString(), any(), any());
    }

    @Test
    void createReportBetween_withNullFrom_shouldThrowException() {
        // Arrange
        String subjectRef = "Patient/patient-123";
        LocalDate to = LocalDate.now();

        // Act & Assert
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> reportService.createReportBetween(subjectRef, null, to, false)
        );
        assertEquals("400 BAD_REQUEST \"from and to are required\"", exception.getMessage());
    }

    @Test
    void createReportBetween_withNullTo_shouldThrowException() {
        // Arrange
        String subjectRef = "Patient/patient-123";
        LocalDate from = LocalDate.now();

        // Act & Assert
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> reportService.createReportBetween(subjectRef, from, null, false)
        );
        assertEquals("400 BAD_REQUEST \"from and to are required\"", exception.getMessage());
    }

    @Test
    void createReportBetween_withSingleDayRange_shouldWork() throws Exception {
        // Arrange
        String subjectRef = "Patient/patient-123";
        LocalDate date = LocalDate.parse("2024-01-15");

        Instant fromInstant = date.atStartOfDay(ZoneId.systemDefault()).toInstant();
        Instant toInstant = date.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant();

        when(submissionRepo.findAllBySubjectRefAndFilledAtGreaterThanEqualAndFilledAtLessThanOrderByFilledAtAsc(
                eq(subjectRef), eq(fromInstant), eq(toInstant)))
                .thenReturn(Collections.singletonList(submission1));

        when(patientRepo.findByPatientId("patient-123"))
                .thenReturn(Optional.of(patient));

        // FIX: Use thenReturn() instead of doNothing()
        when(fhirReportService.createAndStoreDiagnosticReport(any(SleepReportResponse.class), anyString()))
                .thenReturn(null);

        // Act
        SleepReportResponse response = reportService.createReportBetween(
                subjectRef, date, date, false);

        // Assert
        assertNotNull(response);
        assertEquals(date, response.from);
        assertEquals(date, response.to);
        assertEquals(1, response.expectedDays);
        assertEquals(1, response.daysAvailable);

        verify(submissionRepo).findAllBySubjectRefAndFilledAtGreaterThanEqualAndFilledAtLessThanOrderByFilledAtAsc(
                eq(subjectRef), eq(fromInstant), eq(toInstant));
    }

    // ============ Helper Method Tests ============
//    @Test
//    void testComputeSleepMinutes() throws Exception {
//        // Arrange
//        String sleepTime = "22:30";
//        String wakeTime = "07:15";
//
//        // Act
//        Integer result = reportService.computeSleepMinutes(sleepTime, wakeTime);
//
//        // Assert
//        assertNotNull(result);
//        assertEquals(525, result); // 8 hours 45 minutes = 525 minutes
//    }
//
//    @Test
//    void testComputeSleepMinutes_withInvalidTimes_shouldReturnNull() throws Exception {
//        // Act & Assert
//        assertNull(reportService.computeSleepMinutes("invalid", "07:15"));
//        assertNull(reportService.computeSleepMinutes("22:30", "invalid"));
//        assertNull(reportService.computeSleepMinutes(null, "07:15"));
//        assertNull(reportService.computeSleepMinutes("22:30", null));
//    }
//
//    @Test
//    void testParseClockMinutes() throws Exception {
//        // Arrange
//        String time = "22:30";
//
//        // Act
//        Integer result = reportService.parseClockMinutes(time);
//
//        // Assert
//        assertNotNull(result);
//        assertEquals(1350, result); // 22 * 60 + 30 = 1350
//    }
//
//    @Test
//    void testParseClockMinutes_withInvalidFormat_shouldReturnNull() throws Exception {
//        // Act & Assert
//        assertNull(reportService.parseClockMinutes("invalid"));
//        assertNull(reportService.parseClockMinutes(""));
//        assertNull(reportService.parseClockMinutes(null));
//        assertNull(reportService.parseClockMinutes("25:70")); // Invalid hours/minutes
//    }
//
//    @Test
//    void testResolvePatientLabel_withPatientData_shouldReturnName() {
//        // Arrange
//        String subjectRef = "Patient/patient-123";
//
//        when(patientRepo.findByPatientId("patient-123"))
//                .thenReturn(Optional.of(patient));
//
//        // Act
//        String result = reportService.resolvePatientLabel(subjectRef);
//
//        // Assert
//        assertEquals("Mustermann Max", result);
//        verify(patientRepo).findByPatientId("patient-123");
//    }
//
//    @Test
//    void testResolvePatientLabel_withoutPatientData_shouldReturnSubjectRef() {
//        // Arrange
//        String subjectRef = "Patient/unknown";
//
//        when(patientRepo.findByPatientId("unknown"))
//                .thenReturn(Optional.empty());
//
//        // Act
//        String result = reportService.resolvePatientLabel(subjectRef);
//
//        // Assert
//        assertEquals(subjectRef, result);
//        verify(patientRepo).findByPatientId("unknown");
//    }
//
//    @Test
//    void testResolvePatientLabel_withNullSubjectRef_shouldReturnDash() {
//        // Act
//        String result = reportService.resolvePatientLabel(null);
//
//        // Assert
//        assertEquals("-", result);
//    }
//
//    @Test
//    void testResolvePatientLabel_withEmptySubjectRef_shouldReturnDash() {
//        // Act
//        String result = reportService.resolvePatientLabel("");
//
//        // Assert
//        assertEquals("-", result);
//    }
//
//    // ============ parseAnswers Tests ============
//    @Test
//    void testParseAnswers_withArrayFormat() throws Exception {
//        // Arrange
//        String json = "["
//                + "{\"id\":0,\"result\":\"3.0\"},"
//                + "{\"id\":1,\"result\":\"Ja\"},"
//                + "{\"id\":2,\"result\":\"22:30\"}"
//                + "]";
//
//        // Act
//        Map<Integer, Object> result = reportService.parseAnswers(json);
//
//        // Assert
//        assertNotNull(result);
//        assertEquals(3, result.size());
//        assertEquals("3.0", result.get(0));
//        assertEquals("Ja", result.get(1));
//        assertEquals("22:30", result.get(2));
//    }
//
//    @Test
//    void testParseAnswers_withObjectFormat() throws Exception {
//        // Arrange
//        String json = "{\"0\":\"3.0\",\"1\":\"Ja\",\"2\":\"22:30\"}";
//
//        // Act
//        Map<Integer, Object> result = reportService.parseAnswers(json);
//
//        // Assert
//        assertNotNull(result);
//        assertEquals(3, result.size());
//        assertEquals("3.0", result.get(0));
//        assertEquals("Ja", result.get(1));
//        assertEquals("22:30", result.get(2));
//    }
//
//    @Test
//    void testParseAnswers_withInvalidJson_shouldReturnEmptyMap() throws Exception {
//        // Arrange
//        String json = "invalid json";
//
//        // Act
//        Map<Integer, Object> result = reportService.parseAnswers(json);
//
//        // Assert
//        assertNotNull(result);
//        assertTrue(result.isEmpty());
//    }
//
//    @Test
//    void testParseAnswers_withNull_shouldReturnEmptyMap() throws Exception {
//        // Act
//        Map<Integer, Object> result = reportService.parseAnswers(null);
//
//        // Assert
//        assertNotNull(result);
//        assertTrue(result.isEmpty());
//    }
//
//    // ============ Helper Methods ============
//    private ProtocolSubmissionEntity createSubmissionForDate(String isoDateTime) {
//        ProtocolSubmissionEntity entity = new ProtocolSubmissionEntity();
//        entity.setId(UUID.randomUUID());
//        entity.setSubjectRef("Patient/patient-123");
//        entity.setTemplateKey("sleep_protocol_v1");
//        entity.setLocale("de-DE");
//        entity.setFilledAt(Instant.parse(isoDateTime));
//        entity.setCreatedAt(Instant.parse(isoDateTime).plusSeconds(300));
//
//        String answersJson = "["
//                + "{\"id\":2,\"result\":\"23:00\"},"
//                + "{\"id\":3,\"result\":\"07:00\"}"
//                + "]";
//        entity.setAnswersJson(answersJson);
//
//        return entity;
//    }
//
}