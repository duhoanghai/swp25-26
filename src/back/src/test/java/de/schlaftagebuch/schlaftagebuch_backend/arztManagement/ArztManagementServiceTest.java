package de.schlaftagebuch.schlaftagebuch_backend.arztManagement;


import de.schlaftagebuch.arztManagement.ArztManagementService;
import de.schlaftagebuch.arztManagement.dto.CodeInfoResponse;
import de.schlaftagebuch.arztManagement.dto.PatientSummaryResponse;
import de.schlaftagebuch.arztManagement.model.OneTimeCodeEntity;
import de.schlaftagebuch.auth.model.Role;
import de.schlaftagebuch.auth.model.UserAccountEntity;
import de.schlaftagebuch.arztManagement.repository.OneTimeCodeRepository;
import de.schlaftagebuch.auth.repository.PatientRepository;
import de.schlaftagebuch.auth.repository.UserAccountRepository;
import de.schlaftagebuch.auth.dto.PatientEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ArztManagementServiceTest {

    @Mock
    private UserAccountRepository userRepo;

    @Mock
    private OneTimeCodeRepository codeRepo;

    @Mock
    private PatientRepository patientRepo;

    @InjectMocks
    private ArztManagementService service;

    private UserAccountEntity mockPatientAccount;
    private PatientEntity mockPatient;
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
        mockPatient.setBirthDate(LocalDate.of(1990, 1, 1));

        // Mock Code
        mockCode = new OneTimeCodeEntity();
        mockCode.setId(UUID.randomUUID());
        mockCode.setCode("ABCD1234");
        mockCode.setPatientRef("Patient/123");
        mockCode.setCreatedAt(Instant.now());
        mockCode.setExpiresAt(Instant.now().plusSeconds(3600));
        mockCode.setAppointmentDate(LocalDate.now());
    }

    @Test
    void listPatients_withPatients_shouldReturnList() {
        // Arrange
        when(userRepo.findAllByRole(Role.PATIENT))
                .thenReturn(List.of(mockPatientAccount));
        when(patientRepo.findByPatientId("123"))
                .thenReturn(Optional.of(mockPatient));

        // Act
        List<PatientSummaryResponse> result = service.listPatients();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());

        PatientSummaryResponse response = result.get(0);
        assertEquals("testuser", response.getUsername());
        assertEquals("Patient/123", response.getSubjectRef());
        assertEquals("123", response.getPatientId());
        assertEquals("Max", response.getGivenName());
        assertEquals("Mustermann", response.getFamilyName());
        assertEquals("Mustermann, Max", response.getDisplayName());

        verify(userRepo).findAllByRole(Role.PATIENT);
        verify(patientRepo).findByPatientId("123");
    }

    @Test
    void listPatients_withNoPatients_shouldReturnEmptyList() {
        // Arrange
        when(userRepo.findAllByRole(Role.PATIENT))
                .thenReturn(Collections.emptyList());

        // Act
        List<PatientSummaryResponse> result = service.listPatients();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(userRepo).findAllByRole(Role.PATIENT);
        verify(patientRepo, never()).findByPatientId(anyString());
    }

    @Test
    void listPatients_patientNotFoundInPatientTable_shouldSetNullValues() {
        // Arrange
        when(userRepo.findAllByRole(Role.PATIENT))
                .thenReturn(List.of(mockPatientAccount));
        when(patientRepo.findByPatientId("123"))
                .thenReturn(Optional.empty());

        // Act
        List<PatientSummaryResponse> result = service.listPatients();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());

        PatientSummaryResponse response = result.get(0);
        assertNull(response.getGivenName());
        assertNull(response.getFamilyName());
        assertEquals("-", response.getDisplayName());
    }

    @Test
    void listPatients_withInvalidSubjectRef_shouldHandleGracefully() {
        // Arrange
        UserAccountEntity account = new UserAccountEntity();
        account.setUsername("invalid");
        account.setRole(Role.PATIENT);
        account.setSubjectRef("InvalidFormat");

        when(userRepo.findAllByRole(Role.PATIENT))
                .thenReturn(List.of(account));

        // Act
        List<PatientSummaryResponse> result = service.listPatients();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("-", result.get(0).getDisplayName());
    }
//Bestanden, aber die extractPatientId ist private Methode
//    @Test
//    void extractPatientId_withValidFormat_shouldExtractId() {
//        // Act
//        String result = service.extractPatientId("Patient/123");
//
//        // Assert
//        assertEquals("123", result);
//    }
//Bestanden, aber die extractPatientId ist private Methode
//    @Test
//    void extractPatientId_withNull_shouldReturnNull() {
//        // Act
//        String result = service.extractPatientId(null);
//
//        // Assert
//        assertNull(result);
//    }
//    Bestanden, aber die extractPatientId ist private Methode
//    @Test
//    void extractPatientId_withInvalidFormat_shouldReturnNull() {
//        // Act
//        String result = service.extractPatientId("InvalidFormat");
//
//        // Assert
//        assertNull(result);
//    }
//    Bestanden, aber die buildDisplayName ist private Methode
//    @Test
//    void buildDisplayName_withFullName_shouldReturnFormatted() {
//        // Arrange
//        PatientSummaryResponse response = new PatientSummaryResponse();
//        response.setGivenName("Max");
//        response.setFamilyName("Mustermann");
//
//        // Act
//        String result = service.buildDisplayName(response);
//
//        // Assert
//        assertEquals("Mustermann, Max", result);
//    }
//Bestanden, aber die buildDisplayName ist private Methode
//    @Test
//    void buildDisplayName_withOnlyGivenName_shouldReturnGivenName() {
//        // Arrange
//        PatientSummaryResponse response = new PatientSummaryResponse();
//        response.setGivenName("Max");
//        response.setFamilyName(null);
//
//        // Act
//        String result = service.buildDisplayName(response);
//
//        // Assert
//        assertEquals("Max", result);
//    }
//Bestanden, aber die buildDisplayName ist private Methode
//
//    @Test
//    void buildDisplayName_withOnlyFamilyName_shouldReturnFamilyName() {
//        // Arrange
//        PatientSummaryResponse response = new PatientSummaryResponse();
//        response.setGivenName(null);
//        response.setFamilyName("Mustermann");
//
//        // Act
//        String result = service.buildDisplayName(response);
//
//        // Assert
//        assertEquals("Mustermann", result);
//    }
//
//    @Test
//    void buildDisplayName_withNoNames_shouldReturnDash() {
//        // Arrange
//        PatientSummaryResponse response = new PatientSummaryResponse();
//
//        // Act
//        String result = service.buildDisplayName(response);
//
//        // Assert
//        assertEquals("-", result);
//    }

    @Test
    void listCodes_shouldReturnAllCodes() {
        // Arrange
        when(codeRepo.findAllByOrderByCreatedAtDesc())
                .thenReturn(List.of(mockCode));

        // Act
        List<CodeInfoResponse> result = service.listCodes();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());

        CodeInfoResponse response = result.get(0);
        assertEquals(mockCode.getId(), response.id);
        assertEquals(mockCode.getCode(), response.code);
        assertEquals("ACTIVE", response.status);

        verify(codeRepo).findAllByOrderByCreatedAtDesc();
    }

    @Test
    void listCodes_withNoCodes_shouldReturnEmptyList() {
        // Arrange
        when(codeRepo.findAllByOrderByCreatedAtDesc())
                .thenReturn(Collections.emptyList());

        // Act
        List<CodeInfoResponse> result = service.listCodes();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void deactivateCode_codeFound_shouldRevoke() {
        // Arrange
        UUID codeId = mockCode.getId();
        when(codeRepo.findById(codeId))
                .thenReturn(Optional.of(mockCode));
        when(codeRepo.save(any(OneTimeCodeEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        CodeInfoResponse result = service.deactivateCode(codeId);

        // Assert
        assertNotNull(result);
        assertEquals("REVOKED", result.status);
        assertNotNull(mockCode.getRevokedAt());

        verify(codeRepo).findById(codeId);
        verify(codeRepo).save(mockCode);
    }

    @Test
    void deactivateCode_codeAlreadyRevoked_shouldKeepRevoked() {
        // Arrange
        UUID codeId = mockCode.getId();
        Instant alreadyRevoked = Instant.now().minusSeconds(100);
        mockCode.setRevokedAt(alreadyRevoked);

        when(codeRepo.findById(codeId))
                .thenReturn(Optional.of(mockCode));

        // Act
        CodeInfoResponse result = service.deactivateCode(codeId);

        // Assert
        assertNotNull(result);
        assertEquals("REVOKED", result.status);
        assertEquals(alreadyRevoked, result.revokedAt);

        verify(codeRepo).findById(codeId);
        verify(codeRepo, never()).save(any());
    }

    @Test
    void deactivateCode_codeNotFound_shouldThrowException() {
        // Arrange
        UUID codeId = UUID.randomUUID();
        when(codeRepo.findById(codeId))
                .thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.deactivateCode(codeId)
        );

        assertTrue(exception.getMessage().contains("Code not found"));
        verify(codeRepo, never()).save(any());
    }
}
//Bestanden, aber die toCodeInfo ist Private
//    @Test
//    void toCodeInfo_withExpiredCode_shouldSetStatusExpired() {
//        // Arrange
//        mockCode.setExpiresAt(Instant.now().minusSeconds(100));
//        Instant now = Instant.now();
//
//        // Act
//        CodeInfoResponse result = service.toCodeInfo(mockCode, now);
//
//        // Assert
//        assertEquals("EXPIRED", result.status);
//    }
//
//    @Test
//    void toCodeInfo_withUsedCode_shouldSetStatusUsed() {
//        // Arrange
//        mockCode.setUsedAt(Instant.now());
//        Instant now = Instant.now();
//
//        // Act
//        CodeInfoResponse result = service.toCodeInfo(mockCode, now);
//
//        // Assert
//        assertEquals("USED", result.status);
//    }
//
//    @Test
//    void toCodeInfo_withRevokedCode_shouldSetStatusRevoked() {
//        // Arrange
//        mockCode.setRevokedAt(Instant.now());
//        Instant now = Instant.now();
//
//        // Act
//        CodeInfoResponse result = service.toCodeInfo(mockCode, now);
//
//        // Assert
//        assertEquals("REVOKED", result.status);
//    }
//
//    @Test
//    void toCodeInfo_withActiveCode_shouldSetStatusActive() {
//        // Arrange
//        Instant now = Instant.now();
//
//        // Act
//        CodeInfoResponse result = service.toCodeInfo(mockCode, now);
//
//        // Assert
//        assertEquals("ACTIVE", result.status);
//    }
//}
