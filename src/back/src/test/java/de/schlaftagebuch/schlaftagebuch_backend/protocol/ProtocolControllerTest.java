package de.schlaftagebuch.schlaftagebuch_backend.protocol;



import de.schlaftagebuch.protocol.ProtocolController;
import de.schlaftagebuch.protocol.ProtocolService;
import de.schlaftagebuch.protocol.dto.ProtocolSubmissionRequest;
import de.schlaftagebuch.protocol.dto.ProtocolSubmissionResponse;
import de.schlaftagebuch.protocol.model.ProtocolSubmissionEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProtocolControllerTest {

    @Mock
    private ProtocolService protocolService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private ProtocolController protocolController;

    private ProtocolSubmissionRequest validRequest;
    private ProtocolSubmissionEntity savedEntity;
    private UUID submissionId;

    @BeforeEach
    void setUp() {
        submissionId = UUID.randomUUID();

        // Create answer DTOs
        ProtocolSubmissionRequest.AnswerDto answer1 = new ProtocolSubmissionRequest.AnswerDto();
        answer1.id = 101;
        answer1.result = "22:30";

        ProtocolSubmissionRequest.AnswerDto answer2 = new ProtocolSubmissionRequest.AnswerDto();
        answer2.id = 102;
        answer2.result = "8";

        // Valid Request
        validRequest = new ProtocolSubmissionRequest();
        validRequest.templateKey = "sleep_protocol_v1";
        validRequest.locale = "de-DE";
        validRequest.filledAt = Instant.parse("2024-01-15T10:30:00Z");
        validRequest.answers = Arrays.asList(answer1, answer2);

        // Saved Entity - Patient als Standard
        savedEntity = new ProtocolSubmissionEntity();
        savedEntity.setId(submissionId);
        savedEntity.setSubjectRef("Patient/123");
        savedEntity.setTemplateKey("sleep_protocol_v1");
        savedEntity.setLocale("de-DE");
        savedEntity.setFilledAt(Instant.parse("2024-01-15T10:30:00Z"));
        savedEntity.setCreatedAt(Instant.parse("2024-01-15T10:35:00Z"));
        savedEntity.setAnswersJson("[{\"id\":101,\"result\":\"22:30\"},{\"id\":102,\"result\":\"8\"}]");
    }

    // ============ Erfolgreiche Tests ============
    @Test
    void submit_withValidAuthenticationAndRequest_shouldReturnResponse() throws Exception {
        // Arrange
        String subjectRef = "Patient/123";
        when(authentication.getName()).thenReturn(subjectRef);
        when(protocolService.submit(subjectRef, validRequest)).thenReturn(savedEntity);

        // Act
        ProtocolSubmissionResponse response = protocolController.submit(authentication, validRequest);

        // Assert
        assertNotNull(response);
        assertEquals(submissionId, response.submissionId);
        assertEquals("sleep_protocol_v1", response.templateKey);
        assertEquals("de-DE", response.locale);
        assertEquals(Instant.parse("2024-01-15T10:30:00Z"), response.filledAt);
        assertEquals(Instant.parse("2024-01-15T10:35:00Z"), response.storedAt);

        verify(authentication).getName();
        verify(protocolService).submit(subjectRef, validRequest);
    }

    @Test
    void submit_withPractitionerAuthentication_shouldWork() throws Exception {
        // Arrange
        String subjectRef = "Practitioner/456";

        // Erstelle eine NEUE Entity für Practitioner
        ProtocolSubmissionEntity practitionerEntity = createProtocolEntity(
                submissionId,
                subjectRef,
                "sleep_protocol_v1",
                "de-DE",
                Instant.parse("2024-01-15T10:30:00Z"),
                Instant.parse("2024-01-15T10:35:00Z")
        );

        when(authentication.getName()).thenReturn(subjectRef);
        when(protocolService.submit(subjectRef, validRequest)).thenReturn(practitionerEntity);

        // Act
        ProtocolSubmissionResponse response = protocolController.submit(authentication, validRequest);

        // Assert
        assertNotNull(response);
        assertEquals(submissionId, response.submissionId);
        assertEquals("sleep_protocol_v1", response.templateKey);
        assertEquals("de-DE", response.locale);

        verify(authentication).getName();
        verify(protocolService).submit(subjectRef, validRequest);
    }

    @Test
    void submit_withUsernameAuthentication_shouldWork() throws Exception {
        // Arrange
        String username = "PAT12345";

        // Erstelle eine NEUE Entity mit Username als subjectRef
        ProtocolSubmissionEntity usernameEntity = createProtocolEntity(
                submissionId,
                username,
                "sleep_protocol_v1",
                "de-DE",
                Instant.parse("2024-01-15T10:30:00Z"),
                Instant.parse("2024-01-15T10:35:00Z")
        );

        when(authentication.getName()).thenReturn(username);
        when(protocolService.submit(username, validRequest)).thenReturn(usernameEntity);

        // Act
        ProtocolSubmissionResponse response = protocolController.submit(authentication, validRequest);

        // Assert
        assertNotNull(response);
        assertEquals(submissionId, response.submissionId);
        verify(authentication).getName();
        verify(protocolService).submit(username, validRequest);
    }

    @Test
    void submit_withDifferentRequestData_shouldCallServiceCorrectly() throws Exception {
        // Arrange
        String subjectRef = "Patient/123";

        // Test mit unterschiedlichen Requests
        ProtocolSubmissionRequest request1 = createRequest("template1", "de-DE", createAnswers(1, "Ja"));
        ProtocolSubmissionRequest request2 = createRequest("template2", "en-US", createAnswers(
                createAnswer(1, "5"),
                createAnswer(2, "Good sleep")
        ));

        ProtocolSubmissionEntity entity1 = createProtocolEntity(
                UUID.randomUUID(),
                subjectRef,
                "template1",
                "de-DE",
                Instant.now(),
                Instant.now()
        );

        ProtocolSubmissionEntity entity2 = createProtocolEntity(
                UUID.randomUUID(),
                subjectRef,
                "template2",
                "en-US",
                Instant.now(),
                Instant.now()
        );

        when(authentication.getName()).thenReturn(subjectRef);
        when(protocolService.submit(subjectRef, request1)).thenReturn(entity1);
        when(protocolService.submit(subjectRef, request2)).thenReturn(entity2);

        // Act & Assert für Request 1
        ProtocolSubmissionResponse response1 = protocolController.submit(authentication, request1);
        assertNotNull(response1);
        assertEquals("template1", response1.templateKey);

        // Act & Assert für Request 2
        ProtocolSubmissionResponse response2 = protocolController.submit(authentication, request2);
        assertNotNull(response2);
        assertEquals("template2", response2.templateKey);

        verify(authentication, times(2)).getName();
        verify(protocolService).submit(subjectRef, request1);
        verify(protocolService).submit(subjectRef, request2);
    }

    // ============ Fehlerfälle ============
    @Test
    void submit_whenServiceThrowsException_shouldPropagate() throws Exception {
        // Arrange
        String subjectRef = "Patient/123";
        when(authentication.getName()).thenReturn(subjectRef);
        when(protocolService.submit(subjectRef, validRequest))
                .thenThrow(new IllegalArgumentException("Invalid user"));

        // Act & Assert
        Exception exception = assertThrows(
                Exception.class,
                () -> protocolController.submit(authentication, validRequest)
        );
        assertEquals("Invalid user", exception.getMessage());

        verify(authentication).getName();
        verify(protocolService).submit(subjectRef, validRequest);
    }

    // ============ Response Structure Tests ============
    @Test
    void submit_shouldReturnCorrectResponseStructure() throws Exception {
        // Arrange
        String subjectRef = "Patient/123";
        when(authentication.getName()).thenReturn(subjectRef);
        when(protocolService.submit(subjectRef, validRequest)).thenReturn(savedEntity);

        // Act
        ProtocolSubmissionResponse response = protocolController.submit(authentication, validRequest);

        // Assert
        assertEquals(savedEntity.getId(), response.submissionId);
        assertEquals(savedEntity.getTemplateKey(), response.templateKey);
        assertEquals(savedEntity.getLocale(), response.locale);
        assertEquals(savedEntity.getFilledAt(), response.filledAt);
        assertEquals(savedEntity.getCreatedAt(), response.storedAt);

        verify(authentication).getName();
        verify(protocolService).submit(subjectRef, validRequest);
    }

    @Test
    void fromEntity_shouldCreateCorrectResponse() {
        // Test the static factory method directly
        // Act
        ProtocolSubmissionResponse response = ProtocolSubmissionResponse.fromEntity(savedEntity);

        // Assert
        assertNotNull(response);
        assertEquals(submissionId, response.submissionId);
        assertEquals("sleep_protocol_v1", response.templateKey);
        assertEquals("de-DE", response.locale);
        assertEquals(Instant.parse("2024-01-15T10:30:00Z"), response.filledAt);
        assertEquals(Instant.parse("2024-01-15T10:35:00Z"), response.storedAt);
    }

    @Test
    void fromEntity_withNullEntity_shouldThrowException() {
        // Act & Assert
        assertThrows(
                NullPointerException.class,
                () -> ProtocolSubmissionResponse.fromEntity(null)
        );
    }

    // ============ Helper Methods ============
    private ProtocolSubmissionEntity createProtocolEntity(
            UUID id,
            String subjectRef,
            String templateKey,
            String locale,
            Instant filledAt,
            Instant createdAt) {
        ProtocolSubmissionEntity entity = new ProtocolSubmissionEntity();
        entity.setId(id);
        entity.setSubjectRef(subjectRef);
        entity.setTemplateKey(templateKey);
        entity.setLocale(locale);
        entity.setFilledAt(filledAt);
        entity.setCreatedAt(createdAt);
        entity.setAnswersJson("[{\"id\":101,\"result\":\"22:30\"}]");
        return entity;
    }

    private ProtocolSubmissionRequest createRequest(String templateKey, String locale,
                                                    List<ProtocolSubmissionRequest.AnswerDto> answers) {
        ProtocolSubmissionRequest request = new ProtocolSubmissionRequest();
        request.templateKey = templateKey;
        request.locale = locale;
        request.filledAt = Instant.now();
        request.answers = answers;
        return request;
    }

    private ProtocolSubmissionRequest.AnswerDto createAnswer(int id, String result) {
        ProtocolSubmissionRequest.AnswerDto answer = new ProtocolSubmissionRequest.AnswerDto();
        answer.id = id;
        answer.result = result;
        return answer;
    }

    private List<ProtocolSubmissionRequest.AnswerDto> createAnswers(int id, String result) {
        return Arrays.asList(createAnswer(id, result));
    }

    private List<ProtocolSubmissionRequest.AnswerDto> createAnswers(ProtocolSubmissionRequest.AnswerDto... answers) {
        return Arrays.asList(answers);
    }
}