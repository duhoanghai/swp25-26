package de.schlaftagebuch.schlaftagebuch_backend.arztManagement;

import de.schlaftagebuch.arztManagement.ArztDashboardService;
import de.schlaftagebuch.arztManagement.dto.DashboardStatsResponse;
import de.schlaftagebuch.arztManagement.dto.PatientDetailResponse;
import de.schlaftagebuch.arztManagement.dto.PatientReportRowResponse;
import de.schlaftagebuch.arztManagement.model.OneTimeCodeEntity;
import de.schlaftagebuch.auth.dto.PatientEntity;
import de.schlaftagebuch.report.model.ReportEntity;
import de.schlaftagebuch.auth.model.Role;
import de.schlaftagebuch.auth.model.UserAccountEntity;
import de.schlaftagebuch.arztManagement.repository.OneTimeCodeRepository;
import de.schlaftagebuch.report.repository.ReportRepository;
import de.schlaftagebuch.auth.repository.UserAccountRepository;
import de.schlaftagebuch.auth.repository.PatientRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ArztDashboardServiceTest {

    @Mock
    private UserAccountRepository userRepo;

    @Mock
    private ReportRepository reportRepo;

    @Mock
    private OneTimeCodeRepository codeRepo;

    @Mock
    private PatientRepository patientRepo;

    @InjectMocks
    private ArztDashboardService service;

    private UserAccountEntity mockPatientAccount;
    private PatientEntity mockPatient;
    private ReportEntity mockReport;
    private OneTimeCodeEntity mockCode;

    @BeforeEach
    void setUp() {
        // Mock Patient Account
        mockPatientAccount = new UserAccountEntity();
        mockPatientAccount.setUsername("testuser");
        mockPatientAccount.setRole(Role.PATIENT);
        mockPatientAccount.setSubjectRef("Patient/123");

        // Mock Patient Entity
        mockPatient = new PatientEntity();
        mockPatient.setPatientId("123");
        mockPatient.setGivenName("Max");
        mockPatient.setFamilyName("Mustermann");

        // Mock Report
        mockReport = new ReportEntity();
        mockReport.setSubjectRef("Patient/123");
        mockReport.setCreatedAt(Instant.now());
        mockReport.setReportType("DAILY");

        // Mock Code
        mockCode = new OneTimeCodeEntity();
        mockCode.setId(java.util.UUID.randomUUID());
        mockCode.setCode("ABCD1234");
        mockCode.setPatientRef("Patient/123");
        mockCode.setCreatedAt(Instant.now());
        mockCode.setExpiresAt(Instant.now().plusSeconds(3600));
        mockCode.setAppointmentDate(LocalDate.now());
    }

    @Test
    void getStats_shouldReturnCorrectCounts() {
        // Arrange
        when(userRepo.countByRole(Role.PATIENT)).thenReturn(5L);
        when(reportRepo.count()).thenReturn(20L);
        when(reportRepo.countByCreatedAtAfter(any(Instant.class))).thenReturn(3L);
        when(reportRepo.findTop5ByOrderByCreatedAtDesc())
                .thenReturn(List.of(mockReport));
        when(userRepo.findBySubjectRef(anyString()))
                .thenReturn(Optional.of(mockPatientAccount));
        when(patientRepo.findByPatientId("123"))
                .thenReturn(Optional.of(mockPatient));

        // Act
        DashboardStatsResponse result = service.getStats();

        // Assert
        assertEquals(5L, result.patientsCount);
        assertEquals(20L, result.reportsCount);
        assertEquals(3L, result.newReportsLast7Days);
        assertNotNull(result.recentReports);
        assertEquals(1, result.recentReports.size());

        verify(userRepo).countByRole(Role.PATIENT);
        verify(reportRepo).count();
        verify(reportRepo).countByCreatedAtAfter(any(Instant.class));
    }

    @Test
    void getStats_withNoRecentReports_shouldReturnEmptyList() {
        // Arrange
        when(userRepo.countByRole(Role.PATIENT)).thenReturn(0L);
        when(reportRepo.count()).thenReturn(0L);
        when(reportRepo.countByCreatedAtAfter(any(Instant.class))).thenReturn(0L);
        when(reportRepo.findTop5ByOrderByCreatedAtDesc())
                .thenReturn(Collections.emptyList());

        // Act
        DashboardStatsResponse result = service.getStats();

        // Assert
        assertEquals(0L, result.patientsCount);
        assertEquals(0L, result.reportsCount);
        assertTrue(result.recentReports.isEmpty());
    }

    @Test
    void listPatientReportRows_shouldReturnSortedList() {
        // Arrange
        UserAccountEntity account1 = new UserAccountEntity();
        account1.setSubjectRef("Patient/1");

        UserAccountEntity account2 = new UserAccountEntity();
        account2.setSubjectRef("Patient/2");

        ReportEntity report1 = new ReportEntity();
        report1.setCreatedAt(Instant.now().minusSeconds(100));

        ReportEntity report2 = new ReportEntity();
        report2.setCreatedAt(Instant.now());

        when(userRepo.findAllByRole(Role.PATIENT))
                .thenReturn(List.of(account1, account2));
        when(reportRepo.countBySubjectRef("Patient/1")).thenReturn(2L);
        when(reportRepo.countBySubjectRef("Patient/2")).thenReturn(1L);
        when(reportRepo.findFirstBySubjectRefOrderByCreatedAtDesc("Patient/1"))
                .thenReturn(Optional.of(report1));
        when(reportRepo.findFirstBySubjectRefOrderByCreatedAtDesc("Patient/2"))
                .thenReturn(Optional.of(report2));
        when(patientRepo.findByPatientId(anyString()))
                .thenReturn(Optional.empty()); // Keine Patientendaten

        // Act
        List<PatientReportRowResponse> result = service.listPatientReportRows();

        // Assert
        assertEquals(2, result.size());
        verify(userRepo).findAllByRole(Role.PATIENT);
    }

    @Test
    void getPatientDetails_patientFound_shouldReturnDetails() {
        // Arrange
        String patientId = "123";
        String subjectRef = "Patient/" + patientId;

        when(userRepo.findBySubjectRef(subjectRef))
                .thenReturn(Optional.of(mockPatientAccount));
        when(reportRepo.countBySubjectRef(subjectRef)).thenReturn(3L);
        when(reportRepo.findFirstBySubjectRefOrderByCreatedAtDesc(subjectRef))
                .thenReturn(Optional.of(mockReport));
        when(codeRepo.findAllByPatientRefOrderByCreatedAtDesc(subjectRef))
                .thenReturn(List.of(mockCode));
        when(patientRepo.findByPatientId(patientId))
                .thenReturn(Optional.of(mockPatient));

        // Act
        PatientDetailResponse result = service.getPatientDetails(patientId);

        // Assert
        assertNotNull(result);
        assertEquals(patientId, result.patientId);
        assertEquals(subjectRef, result.patientRef);
        assertEquals(3L, result.reportsCount);
        assertEquals(mockReport.getCreatedAt(), result.latestReportCreatedAt);
        assertNotNull(result.latestCode);
        assertEquals("ACTIVE", result.latestCodeStatus);

        verify(userRepo).findBySubjectRef(subjectRef);
        verify(reportRepo).countBySubjectRef(subjectRef);
    }
}
// Bestanden, aber nur wenn get PatientDetail public ist
//    @Test
//    void getPatientDetails_patientNotFound_shouldThrowException() {
//        // Arrange
//        String patientId = "999";
//        String subjectRef = "Patient/" + patientId;
//
//        when(userRepo.findBySubjectRef(subjectRef))
//                .thenReturn(Optional.empty());
//
//        // Act & Assert
//        IllegalArgumentException exception = assertThrows(
//                IllegalArgumentException.class,
//                () -> service.getPatientDetails(patientId)
//        );
//
//        assertTrue(exception.getMessage().contains("Patient not found"));
//    }

//    @Test
//    void getPatientDetails_noCodes_shouldReturnPartialDetails() {
//        // Arrange
//        String patientId = "123";
//        String subjectRef = "Patient/" + patientId;
//
//        when(userRepo.findBySubjectRef(subjectRef))
//                .thenReturn(Optional.of(mockPatientAccount));
//        when(reportRepo.countBySubjectRef(subjectRef)).thenReturn(0L);
//        when(reportRepo.findFirstBySubjectRefOrderByCreatedAtDesc(subjectRef))
//                .thenReturn(Optional.empty());
//        when(codeRepo.findAllByPatientRefOrderByCreatedAtDesc(subjectRef))
//                .thenReturn(Collections.emptyList());
//        when(patientRepo.findByPatientId(patientId))
//                .thenReturn(Optional.of(mockPatient));
//
//        // Act
//        PatientDetailResponse result = service.getPatientDetails(patientId);
//
//        // Assert
//        assertNotNull(result);
//        assertEquals(0L, result.reportsCount);
//        assertNull(result.latestReportCreatedAt);
//        assertNull(result.latestCode);
//        assertNull(result.latestCodeStatus);
//    }
//
//    @Test
//    void toPatientDisplay_withValidPatient_shouldReturnFormattedName() {
//        // Arrange
//        when(patientRepo.findByPatientId("123"))
//                .thenReturn(Optional.of(mockPatient));
//
//        // Act
//        String display = service.toPatientDisplay(mockPatientAccount);
//
//        // Assert
//        assertEquals("Mustermann, Max", display);
//    }
//
//    @Test
//    void toPatientDisplay_withNoPatientData_shouldReturnDash() {
//        // Arrange
//        UserAccountEntity account = new UserAccountEntity();
//        account.setSubjectRef("Patient/999");
//
//        when(patientRepo.findByPatientId("999"))
//                .thenReturn(Optional.empty());
//
//        // Act
//        String display = service.toPatientDisplay(account);
//
//        // Assert
//        assertEquals("—", display);
//    }
//
//    @Test
//    void toPatientDisplay_withNullSubjectRef_shouldReturnDash() {
//        // Arrange
//        UserAccountEntity account = new UserAccountEntity();
//        account.setSubjectRef(null);
//
//        // Act
//        String display = service.toPatientDisplay(account);
//
//        // Assert
//        assertEquals("—", display);
//    }
//
//    @Test
//    void toPatientDisplay_withEmptyNames_shouldReturnDash() {
//        // Arrange
//        PatientEntity patient = new PatientEntity();
//        patient.setPatientId("123");
//        patient.setGivenName("");
//        patient.setFamilyName("");
//
//        UserAccountEntity account = new UserAccountEntity();
//        account.setSubjectRef("Patient/123");
//
//        when(patientRepo.findByPatientId("123"))
//                .thenReturn(Optional.of(patient));
//
//        // Act
//        String display = service.toPatientDisplay(account);
//
//        // Assert
//        assertEquals("—", display);
//    }
//}
