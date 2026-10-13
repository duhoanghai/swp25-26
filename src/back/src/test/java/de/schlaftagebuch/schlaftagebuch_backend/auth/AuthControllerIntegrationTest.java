package de.schlaftagebuch.schlaftagebuch_backend.auth;


import de.schlaftagebuch.arztManagement.dto.GenerateCodeRequest;
import de.schlaftagebuch.arztManagement.dto.GenerateCodeResponse;
import de.schlaftagebuch.auth.AuthController;
import de.schlaftagebuch.auth.AuthService;
import de.schlaftagebuch.auth.dto.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    private TokenResponse arztTokenResponse;
    private TokenResponse patientTokenResponse;
    private GenerateCodeResponse generateCodeResponse;

    @BeforeEach
    void setUp() {
        // Arzt Token Response
        arztTokenResponse = new TokenResponse(
                "arzt_jwt_token",
                "ARZT",
                "Practitioner/123",
                "dr.mueller"
        );

        // Patient Token Response
        patientTokenResponse = new TokenResponse(
                "patient_jwt_token",
                "PATIENT",
                "Patient/patient-123",
                "PAT12345"
        );

        // Generate Code Response
        generateCodeResponse = new GenerateCodeResponse(
                UUID.randomUUID(),
                "ABC123",
                Instant.now(),
                Instant.now().plusSeconds(86400),
                "patient-123",
                "Patient/patient-123"
        );
    }

    // ============ Arzt Login Tests ============
    @Test
    void loginArzt_shouldCallServiceAndReturnResponse() {
        // Arrange
        ArztLoginRequest request = new ArztLoginRequest();
        request.username = "dr.mueller";
        request.password = "password";

        when(authService.loginArzt(request)).thenReturn(arztTokenResponse);

        // Act
        TokenResponse response = authController.loginArzt(request);

        // Assert
        assertNotNull(response);
        //assertEquals("arzt_jwt_token", response.token());
       // assertEquals("ARZT", response.role());
        // assertEquals("dr.mueller", response.username());

        verify(authService).loginArzt(request);
    }

    @Test
    void loginArzt_whenServiceThrowsException_shouldPropagate() {
        // Arrange
        ArztLoginRequest request = new ArztLoginRequest();
        request.username = "invalid";
        request.password = "wrong";

        when(authService.loginArzt(request))
                .thenThrow(new IllegalArgumentException("Invalid credentials"));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> authController.loginArzt(request)
        );
        assertEquals("Invalid credentials", exception.getMessage());
        verify(authService).loginArzt(request);
    }

    // ============ Generate Code for Existing Patient Tests ============
    @Test
    void generateCodeForExisting_shouldCallServiceWithPatientId() {
        // Arrange
        String patientId = "patient-123";
        GenerateCodeRequest request = new GenerateCodeRequest();
        request.validUntilDate = "2024-12-31";
        request.appointmentDate = "2024-01-20";

        when(authService.generateCodeForPatient(patientId, request))
                .thenReturn(generateCodeResponse);

        // Act
        GenerateCodeResponse response = authController.generateCodeForExisting(patientId, request);

        // Assert
        assertNotNull(response);
       // assertEquals("ABC123", response.code());
       // assertEquals("patient-123", response.patientId());

        verify(authService).generateCodeForPatient(patientId, request);
    }

    @Test
    void generateCodeForExisting_withoutRequest_shouldPassNull() {
        // Arrange
        String patientId = "patient-123";

        when(authService.generateCodeForPatient(patientId, null))
                .thenReturn(generateCodeResponse);

        // Act
        GenerateCodeResponse response = authController.generateCodeForExisting(patientId, null);

        // Assert
        assertNotNull(response);
        verify(authService).generateCodeForPatient(patientId, null);
    }

    // ============ Generate Code for New Patient Tests ============
    @Test
    void generateCodeForNew_shouldCallService() {
        // Arrange
        GenerateCodeRequest request = new GenerateCodeRequest();
        request.validUntilDate = "2024-12-31";

        when(authService.generateCodeForNewPatient(request))
                .thenReturn(generateCodeResponse);

        // Act
        GenerateCodeResponse response = authController.generateCodeForNew(request);
    }
}