package de.schlaftagebuch.schlaftagebuch_backend.auth;


import de.schlaftagebuch.arztManagement.dto.GenerateCodeRequest;
import de.schlaftagebuch.arztManagement.dto.GenerateCodeResponse;
import de.schlaftagebuch.arztManagement.model.OneTimeCodeEntity;
import de.schlaftagebuch.arztManagement.repository.OneTimeCodeRepository;
import de.schlaftagebuch.auth.AuthService;
import de.schlaftagebuch.auth.dto.ArztLoginRequest;
import de.schlaftagebuch.auth.dto.PatientEntity;
import de.schlaftagebuch.auth.dto.PatientFirstLoginRequest;
import de.schlaftagebuch.auth.dto.PatientLoginRequest;
import de.schlaftagebuch.auth.dto.TokenResponse;
import de.schlaftagebuch.auth.model.Role;
import de.schlaftagebuch.auth.model.UserAccountEntity;
import de.schlaftagebuch.auth.repository.PatientRepository;
import de.schlaftagebuch.auth.repository.UserAccountRepository;
import de.schlaftagebuch.fhir.store.ConsentStore;
import de.schlaftagebuch.fhir.store.PatientStore;
import de.schlaftagebuch.security.JwtService;
import org.hl7.fhir.r4.model.Consent;
import org.hl7.fhir.r4.model.Patient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AuthService.
 * Tests the business logic in isolation using mocked dependencies.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService Unit Tests")
class AuthServiceTest {

    @Mock
    private UserAccountRepository userRepo;

    @Mock
    private OneTimeCodeRepository codeRepo;

    @Mock
    private PasswordEncoder encoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private PatientStore patientStore;

    @Mock
    private ConsentStore consentStore;

    @Mock
    private PatientRepository patientRepo;

    @InjectMocks
    private AuthService authService;

    private UserAccountEntity arztAccount;
    private UserAccountEntity patientAccount;

    @BeforeEach
    void setUp() {
        // Setup test data
        arztAccount = new UserAccountEntity();
        arztAccount.setId(UUID.randomUUID());
        arztAccount.setUsername("arzt1");
        arztAccount.setPasswordHash("$2a$10$hashedPassword");
        arztAccount.setRole(Role.ARZT);
        arztAccount.setSubjectRef("Practitioner/arzt-uuid");

        patientAccount = new UserAccountEntity();
        patientAccount.setId(UUID.randomUUID());
        patientAccount.setUsername("ABCD1234");
        patientAccount.setPasswordHash("$2a$10$hashedPatientPassword");
        patientAccount.setRole(Role.PATIENT);
        patientAccount.setSubjectRef("Patient/patient-uuid");
    }

    // ===== Arzt Login Tests =====

    @Test
    @DisplayName("Login Arzt - Success")
    void loginArzt_Success() {
        // Arrange
        ArztLoginRequest request = new ArztLoginRequest();
        request.username = "arzt1";
        request.password = "password123";

        when(userRepo.findByUsername("arzt1")).thenReturn(Optional.of(arztAccount));
        when(encoder.matches("password123", arztAccount.getPasswordHash())).thenReturn(true);
        when(jwtService.generateToken(anyString(), any(Role.class), anyString()))
                .thenReturn("jwt-token");

        // Act
        TokenResponse response = authService.loginArzt(request);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.token).isEqualTo("jwt-token");
        assertThat(response.role).isEqualTo("ARZT");
        assertThat(response.subjectRef).isEqualTo("Practitioner/arzt-uuid");
        assertThat(response.username).isEqualTo("arzt1");

        verify(userRepo).findByUsername("arzt1");
        verify(encoder).matches("password123", arztAccount.getPasswordHash());
        verify(jwtService).generateToken("arzt1", Role.ARZT, "Practitioner/arzt-uuid");
    }

    @Test
    @DisplayName("Login Arzt - Invalid Username")
    void loginArzt_InvalidUsername() {
        // Arrange
        ArztLoginRequest request = new ArztLoginRequest();
        request.username = "nonexistent";
        request.password = "password123";

        when(userRepo.findByUsername("nonexistent")).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> authService.loginArzt(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid username/password");

        verify(userRepo).findByUsername("nonexistent");
        verify(encoder, never()).matches(anyString(), anyString());
        verify(jwtService, never()).generateToken(anyString(), any(), anyString());
    }

    @Test
    @DisplayName("Login Arzt - Wrong Password")
    void loginArzt_WrongPassword() {
        // Arrange
        ArztLoginRequest request = new ArztLoginRequest();
        request.username = "arzt1";
        request.password = "wrongPassword";

        when(userRepo.findByUsername("arzt1")).thenReturn(Optional.of(arztAccount));
        when(encoder.matches("wrongPassword", arztAccount.getPasswordHash())).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> authService.loginArzt(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid username/password");

        verify(userRepo).findByUsername("arzt1");
        verify(encoder).matches("wrongPassword", arztAccount.getPasswordHash());
        verify(jwtService, never()).generateToken(anyString(), any(), anyString());
    }

    @Test
    @DisplayName("Login Arzt - Patient Account Cannot Login as Arzt")
    void loginArzt_PatientAccountCannotLogin() {
        // Arrange
        ArztLoginRequest request = new ArztLoginRequest();
        request.username = "ABCD1234";
        request.password = "password123";

        when(userRepo.findByUsername("ABCD1234")).thenReturn(Optional.of(patientAccount));

        // Act & Assert
        assertThatThrownBy(() -> authService.loginArzt(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Not an ARZT account");

        verify(userRepo).findByUsername("ABCD1234");
        verify(encoder, never()).matches(anyString(), anyString());
    }

    // ===== Generate Code for New Patient Tests =====

    @Test
    @DisplayName("Generate Code for New Patient - Success")
    void generateCodeForNewPatient_Success() {
        // Arrange
        GenerateCodeRequest request = new GenerateCodeRequest();
        request.validUntilDate = null; // Default: 24h

        when(codeRepo.findByCode(anyString())).thenReturn(Optional.empty());
        when(codeRepo.save(any(OneTimeCodeEntity.class))).thenAnswer(invocation -> {
            OneTimeCodeEntity entity = invocation.getArgument(0);
            entity.setId(UUID.randomUUID());
            return entity;
        });

        // Act
        GenerateCodeResponse response = authService.generateCodeForNewPatient(request);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.code).hasSize(6);
        assertThat(response.code).matches("[ABCDEFGHJKLMNPQRSTUVWXYZ23456789]{6}");
        assertThat(response.patientId).isNotNull();
        assertThat(response.patientRef).startsWith("Patient/");
        assertThat(response.createdAt).isNotNull();
        assertThat(response.expiresAt).isAfter(response.createdAt);

        verify(patientStore).save(anyString(), any(Patient.class));
        verify(codeRepo).save(any(OneTimeCodeEntity.class));
    }

    @Test
    @DisplayName("Generate Code for New Patient - With Valid Until Date")
    void generateCodeForNewPatient_WithValidUntilDate() {
        // Arrange
        GenerateCodeRequest request = new GenerateCodeRequest();
        request.validUntilDate = "2026-12-31";

        when(codeRepo.findByCode(anyString())).thenReturn(Optional.empty());
        when(codeRepo.save(any(OneTimeCodeEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        GenerateCodeResponse response = authService.generateCodeForNewPatient(request);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.code).isNotNull();

        ArgumentCaptor<OneTimeCodeEntity> captor = ArgumentCaptor.forClass(OneTimeCodeEntity.class);
        verify(codeRepo).save(captor.capture());

        OneTimeCodeEntity savedCode = captor.getValue();
        assertThat(savedCode.getExpiresAt()).isNotNull();
    }

    @Test
    @DisplayName("Generate Code for New Patient - With Appointment Date")
    void generateCodeForNewPatient_WithAppointmentDate() {
        // Arrange
        GenerateCodeRequest request = new GenerateCodeRequest();
        request.appointmentDate = "2026-06-15";

        when(codeRepo.findByCode(anyString())).thenReturn(Optional.empty());
        when(codeRepo.save(any(OneTimeCodeEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        GenerateCodeResponse response = authService.generateCodeForNewPatient(request);

        // Assert
        ArgumentCaptor<OneTimeCodeEntity> captor = ArgumentCaptor.forClass(OneTimeCodeEntity.class);
        verify(codeRepo).save(captor.capture());

        OneTimeCodeEntity savedCode = captor.getValue();
        assertThat(savedCode.getAppointmentDate()).isEqualTo(LocalDate.parse("2026-06-15"));
    }

    // ===== Generate Code for Existing Patient Tests =====

    @Test
    @DisplayName("Generate Code for Existing Patient - Success")
    void generateCodeForExistingPatient_Success() {
        // Arrange
        String patientId = "existing-patient-uuid";
        GenerateCodeRequest request = new GenerateCodeRequest();

        Patient existingPatient = new Patient();
        existingPatient.setId(patientId);

        when(patientStore.findById(patientId)).thenReturn(Optional.of(existingPatient));
        when(codeRepo.findByCode(anyString())).thenReturn(Optional.empty());
        when(codeRepo.save(any(OneTimeCodeEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        GenerateCodeResponse response = authService.generateCodeForPatient(patientId, request);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.patientId).isEqualTo(patientId);
        assertThat(response.patientRef).isEqualTo("Patient/" + patientId);

        verify(patientStore).findById(patientId);
        verify(codeRepo).save(any(OneTimeCodeEntity.class));
    }

    @Test
    @DisplayName("Generate Code for Existing Patient - Patient Not Found")
    void generateCodeForExistingPatient_PatientNotFound() {
        // Arrange
        String patientId = "nonexistent-patient";
        GenerateCodeRequest request = new GenerateCodeRequest();

        when(patientStore.findById(patientId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> authService.generateCodeForPatient(patientId, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Patient not found in FHIR store");

        verify(patientStore).findById(patientId);
        verify(codeRepo, never()).save(any());
    }

    // ===== Patient First Login Tests =====

    @Test
    @DisplayName("Patient First Login - Success")
    void patientFirstLogin_Success() {
        // Arrange
        PatientFirstLoginRequest request = new PatientFirstLoginRequest();
        request.code = "ABC123";
        request.consentAccepted = true;
        request.givenName = "Max";
        request.familyName = "Mustermann";
        request.birthDate = "1990-05-15";
        request.password = "securePassword123";

        String patientId = UUID.randomUUID().toString();
        String patientRef = "Patient/" + patientId;

        OneTimeCodeEntity codeEntity = new OneTimeCodeEntity();
        codeEntity.setCode("ABC123");
        codeEntity.setPatientRef(patientRef);
        codeEntity.setExpiresAt(Instant.now().plusSeconds(3600));
        codeEntity.setUsedAt(null);
        codeEntity.setRevokedAt(null);

        Patient patient = new Patient();
        patient.setId(patientId);

        when(codeRepo.findByCode("ABC123")).thenReturn(Optional.of(codeEntity));
        when(patientStore.findById(patientId)).thenReturn(Optional.of(patient));
        when(encoder.encode("securePassword123")).thenReturn("$2a$10$hashedPassword");
        when(userRepo.existsByUsername(anyString())).thenReturn(false);
        when(userRepo.save(any(UserAccountEntity.class))).thenAnswer(invocation -> {
            UserAccountEntity entity = invocation.getArgument(0);
            entity.setId(UUID.randomUUID());
            return entity;
        });
        when(patientRepo.findByPatientId(patientId)).thenReturn(Optional.empty());
        when(patientRepo.save(any(PatientEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(jwtService.generateToken(anyString(), any(Role.class), anyString())).thenReturn("jwt-token");

        // Act
        TokenResponse response = authService.patientFirstLogin(request);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.token).isEqualTo("jwt-token");
        assertThat(response.role).isEqualTo("PATIENT");
        assertThat(response.subjectRef).isEqualTo(patientRef);
        assertThat(response.username).matches("[ABCDEFGHJKLMNPQRSTUVWXYZ23456789]{8}");

        // Verify patient was updated
        verify(patientStore).save(eq(patientId), any(Patient.class));

        // Verify consent was saved
        verify(consentStore).save(anyString(), any(Consent.class));

        // Verify user account was created
        ArgumentCaptor<UserAccountEntity> userCaptor = ArgumentCaptor.forClass(UserAccountEntity.class);
        verify(userRepo).save(userCaptor.capture());
        UserAccountEntity savedUser = userCaptor.getValue();
        assertThat(savedUser.getRole()).isEqualTo(Role.PATIENT);
        assertThat(savedUser.getSubjectRef()).isEqualTo(patientRef);

        // Verify patient entity was saved
        ArgumentCaptor<PatientEntity> patientCaptor = ArgumentCaptor.forClass(PatientEntity.class);
        verify(patientRepo).save(patientCaptor.capture());
        PatientEntity savedPatient = patientCaptor.getValue();
        assertThat(savedPatient.getGivenName()).isEqualTo("Max");
        assertThat(savedPatient.getFamilyName()).isEqualTo("Mustermann");
        assertThat(savedPatient.getBirthDate()).isEqualTo(LocalDate.parse("1990-05-15"));

        // Verify code was marked as used
        verify(codeRepo).save(argThat(code -> code.getUsedAt() != null));
    }

    @Test
    @DisplayName("Patient First Login - Consent Not Accepted")
    void patientFirstLogin_ConsentNotAccepted() {
        // Arrange
        PatientFirstLoginRequest request = new PatientFirstLoginRequest();
        request.code = "ABC123";
        request.consentAccepted = false;
        request.givenName = "Max";
        request.familyName = "Mustermann";
        request.birthDate = "1990-05-15";
        request.password = "securePassword123";

        // Act & Assert
        assertThatThrownBy(() -> authService.patientFirstLogin(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Consent is required");

        verify(codeRepo, never()).findByCode(anyString());
    }

    @Test
    @DisplayName("Patient First Login - Invalid Code")
    void patientFirstLogin_InvalidCode() {
        // Arrange
        PatientFirstLoginRequest request = new PatientFirstLoginRequest();
        request.code = "INVALID";
        request.consentAccepted = true;
        request.givenName = "Max";
        request.familyName = "Mustermann";
        request.birthDate = "1990-05-15";
        request.password = "securePassword123";

        when(codeRepo.findByCode("INVALID")).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> authService.patientFirstLogin(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid code");

        verify(codeRepo).findByCode("INVALID");
    }

    @Test
    @DisplayName("Patient First Login - Code Already Used")
    void patientFirstLogin_CodeAlreadyUsed() {
        // Arrange
        PatientFirstLoginRequest request = new PatientFirstLoginRequest();
        request.code = "ABC123";
        request.consentAccepted = true;
        request.givenName = "Max";
        request.familyName = "Mustermann";
        request.birthDate = "1990-05-15";
        request.password = "securePassword123";

        OneTimeCodeEntity codeEntity = new OneTimeCodeEntity();
        codeEntity.setCode("ABC123");
        codeEntity.setUsedAt(Instant.now().minusSeconds(3600)); // Already used

        when(codeRepo.findByCode("ABC123")).thenReturn(Optional.of(codeEntity));

        // Act & Assert
        assertThatThrownBy(() -> authService.patientFirstLogin(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Code already used");
    }

    @Test
    @DisplayName("Patient First Login - Code Expired")
    void patientFirstLogin_CodeExpired() {
        // Arrange
        PatientFirstLoginRequest request = new PatientFirstLoginRequest();
        request.code = "ABC123";
        request.consentAccepted = true;
        request.givenName = "Max";
        request.familyName = "Mustermann";
        request.birthDate = "1990-05-15";
        request.password = "securePassword123";

        OneTimeCodeEntity codeEntity = new OneTimeCodeEntity();
        codeEntity.setCode("ABC123");
        codeEntity.setExpiresAt(Instant.now().minusSeconds(3600)); // Expired
        codeEntity.setUsedAt(null);
        codeEntity.setRevokedAt(null);

        when(codeRepo.findByCode("ABC123")).thenReturn(Optional.of(codeEntity));

        // Act & Assert
        assertThatThrownBy(() -> authService.patientFirstLogin(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Code expired");
    }

    @Test
    @DisplayName("Patient First Login - Code Revoked")
    void patientFirstLogin_CodeRevoked() {
        // Arrange
        PatientFirstLoginRequest request = new PatientFirstLoginRequest();
        request.code = "ABC123";
        request.consentAccepted = true;
        request.givenName = "Max";
        request.familyName = "Mustermann";
        request.birthDate = "1990-05-15";
        request.password = "securePassword123";

        OneTimeCodeEntity codeEntity = new OneTimeCodeEntity();
        codeEntity.setCode("ABC123");
        codeEntity.setRevokedAt(Instant.now().minusSeconds(3600)); // Revoked

        when(codeRepo.findByCode("ABC123")).thenReturn(Optional.of(codeEntity));

        // Act & Assert
        assertThatThrownBy(() -> authService.patientFirstLogin(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Code revoked");
    }

    @Test
    @DisplayName("Patient First Login - Missing Birth Date")
    void patientFirstLogin_MissingBirthDate() {
        // Arrange
        PatientFirstLoginRequest request = new PatientFirstLoginRequest();
        request.code = "ABC123";
        request.consentAccepted = true;
        request.givenName = "Max";
        request.familyName = "Mustermann";
        request.birthDate = "";
        request.password = "securePassword123";

        String patientId = UUID.randomUUID().toString();
        OneTimeCodeEntity codeEntity = new OneTimeCodeEntity();
        codeEntity.setCode("ABC123");
        codeEntity.setPatientRef("Patient/" + patientId);
        codeEntity.setExpiresAt(Instant.now().plusSeconds(3600));
        codeEntity.setUsedAt(null);
        codeEntity.setRevokedAt(null);

        when(codeRepo.findByCode("ABC123")).thenReturn(Optional.of(codeEntity));

        // Act & Assert
        assertThatThrownBy(() -> authService.patientFirstLogin(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("birthDate is required (YYYY-MM-DD)");
    }

    @Test
    @DisplayName("Patient First Login - Invalid Birth Date Format")
    void patientFirstLogin_InvalidBirthDateFormat() {
        // Arrange
        PatientFirstLoginRequest request = new PatientFirstLoginRequest();
        request.code = "ABC123";
        request.consentAccepted = true;
        request.givenName = "Max";
        request.familyName = "Mustermann";
        request.birthDate = "15.05.1990"; // Wrong format
        request.password = "securePassword123";

        String patientId = UUID.randomUUID().toString();
        OneTimeCodeEntity codeEntity = new OneTimeCodeEntity();
        codeEntity.setCode("ABC123");
        codeEntity.setPatientRef("Patient/" + patientId);
        codeEntity.setExpiresAt(Instant.now().plusSeconds(3600));
        codeEntity.setUsedAt(null);
        codeEntity.setRevokedAt(null);

        when(codeRepo.findByCode("ABC123")).thenReturn(Optional.of(codeEntity));

        // Act & Assert
        assertThatThrownBy(() -> authService.patientFirstLogin(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("birthDate must be ISO format YYYY-MM-DD");
    }

    // ===== Patient Login Tests =====

    @Test
    @DisplayName("Patient Login - Success")
    void loginPatient_Success() {
        // Arrange
        PatientLoginRequest request = new PatientLoginRequest();
        request.username = "ABCD1234";
        request.password = "password123";

        when(userRepo.findByUsername("ABCD1234")).thenReturn(Optional.of(patientAccount));
        when(encoder.matches("password123", patientAccount.getPasswordHash())).thenReturn(true);
        when(jwtService.generateToken(anyString(), any(Role.class), anyString()))
                .thenReturn("jwt-token");

        // Act
        TokenResponse response = authService.loginPatient(request);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.token).isEqualTo("jwt-token");
        assertThat(response.role).isEqualTo("PATIENT");
        assertThat(response.subjectRef).isEqualTo("Patient/patient-uuid");
        assertThat(response.username).isEqualTo("ABCD1234");

        verify(userRepo).findByUsername("ABCD1234");
        verify(encoder).matches("password123", patientAccount.getPasswordHash());
        verify(jwtService).generateToken("ABCD1234", Role.PATIENT, "Patient/patient-uuid");
    }

    @Test
    @DisplayName("Patient Login - Invalid Username")
    void loginPatient_InvalidUsername() {
        // Arrange
        PatientLoginRequest request = new PatientLoginRequest();
        request.username = "INVALID";
        request.password = "password123";

        when(userRepo.findByUsername("INVALID")).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> authService.loginPatient(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid username/password");

        verify(userRepo).findByUsername("INVALID");
        verify(encoder, never()).matches(anyString(), anyString());
    }

    @Test
    @DisplayName("Patient Login - Wrong Password")
    void loginPatient_WrongPassword() {
        // Arrange
        PatientLoginRequest request = new PatientLoginRequest();
        request.username = "ABCD1234";
        request.password = "wrongPassword";

        when(userRepo.findByUsername("ABCD1234")).thenReturn(Optional.of(patientAccount));
        when(encoder.matches("wrongPassword", patientAccount.getPasswordHash())).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> authService.loginPatient(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid username/password");

        verify(userRepo).findByUsername("ABCD1234");
        verify(encoder).matches("wrongPassword", patientAccount.getPasswordHash());
        verify(jwtService, never()).generateToken(anyString(), any(), anyString());
    }

    @Test
    @DisplayName("Patient Login - Arzt Account Cannot Login as Patient")
    void loginPatient_ArztAccountCannotLogin() {
        // Arrange
        PatientLoginRequest request = new PatientLoginRequest();
        request.username = "arzt1";
        request.password = "password123";

        when(userRepo.findByUsername("arzt1")).thenReturn(Optional.of(arztAccount));

        // Act & Assert
        assertThatThrownBy(() -> authService.loginPatient(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Not a PATIENT account");

        verify(userRepo).findByUsername("arzt1");
        verify(encoder, never()).matches(anyString(), anyString());
    }
}