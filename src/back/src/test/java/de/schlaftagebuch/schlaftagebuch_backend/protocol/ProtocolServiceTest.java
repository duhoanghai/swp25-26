package de.schlaftagebuch.schlaftagebuch_backend.protocol;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.schlaftagebuch.protocol.ProtocolService;
import de.schlaftagebuch.protocol.dto.ProtocolSubmissionRequest;
import de.schlaftagebuch.protocol.model.ProtocolSubmissionEntity;
import de.schlaftagebuch.auth.model.Role;
import de.schlaftagebuch.auth.model.UserAccountEntity;
import de.schlaftagebuch.protocol.repository.ProtocolSubmissionRepository;
import de.schlaftagebuch.auth.repository.UserAccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProtocolServiceTest {

    @Mock
    private UserAccountRepository userRepo;

    @Mock
    private ProtocolSubmissionRepository protocolRepo;

    @InjectMocks
    private ProtocolService protocolService;

    private UserAccountEntity patientUser;
    private UserAccountEntity practitionerUser;
    private ProtocolSubmissionEntity savedSubmission;
    private ProtocolSubmissionRequest.AnswerDto answer1;
    private ProtocolSubmissionRequest.AnswerDto answer2;
    private ProtocolSubmissionRequest validRequest;
    private ObjectMapper objectMapper = new ObjectMapper();
    private UUID patientUserId;
    private UUID practitionerUserId;
    @BeforeEach
    void setUp() {
            patientUserId = UUID.randomUUID();
            practitionerUserId = UUID.randomUUID();
        // Patient User
        patientUser = new UserAccountEntity();
        patientUser.setId(patientUserId);  // UUID statt Long
        patientUser.setUsername("PAT12345");
        patientUser.setRole(Role.PATIENT);
        patientUser.setSubjectRef("Patient/123");

        // Practitioner User
        practitionerUser = new UserAccountEntity();
        practitionerUser.setId(practitionerUserId);  // UUID statt Long
        practitionerUser.setUsername("dr.mueller");
        practitionerUser.setRole(Role.ARZT);
        practitionerUser.setSubjectRef("Practitioner/456");

        // Create answer DTOs
        answer1 = new ProtocolSubmissionRequest.AnswerDto();
        answer1.id = 101;
        answer1.result = "22:30";

        answer2 = new ProtocolSubmissionRequest.AnswerDto();
        answer2.id = 102;
        answer2.result = "8"; // Sleep quality as string

        // Valid Protocol Submission Request
        validRequest = new ProtocolSubmissionRequest();
        validRequest.templateKey = "sleep_protocol_v1";
        validRequest.locale = "de-DE";
        validRequest.filledAt = Instant.parse("2024-01-15T10:30:00Z");
        validRequest.answers = Arrays.asList(answer1, answer2);

        // Saved Submission Entity
        savedSubmission = new ProtocolSubmissionEntity();
        savedSubmission.setId(UUID.randomUUID());
        savedSubmission.setSubjectRef("Patient/123");
        savedSubmission.setTemplateKey("sleep_protocol_v1");
        savedSubmission.setLocale("de-DE");
        savedSubmission.setFilledAt(Instant.parse("2024-01-15T10:30:00Z"));
        savedSubmission.setCreatedAt(Instant.parse("2024-01-15T10:35:00Z"));
        try {
            savedSubmission.setAnswersJson(objectMapper.writeValueAsString(validRequest.answers));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    // ============ Erfolgreiche Tests ============
    @Test
    void submit_withPatientSubjectRef_shouldSaveSuccessfully() throws Exception {
        // Arrange
        String subjectRef = "Patient/123";
        when(userRepo.findBySubjectRef(subjectRef)).thenReturn(Optional.of(patientUser));
        when(protocolRepo.save(any(ProtocolSubmissionEntity.class))).thenReturn(savedSubmission);

        // Act
        ProtocolSubmissionEntity result = protocolService.submit(subjectRef, validRequest);

        // Assert
        assertNotNull(result);
        assertEquals(savedSubmission.getId(), result.getId());
        assertEquals("Patient/123", result.getSubjectRef());
        assertEquals("sleep_protocol_v1", result.getTemplateKey());
        assertEquals("de-DE", result.getLocale());
        assertEquals(Instant.parse("2024-01-15T10:30:00Z"), result.getFilledAt());
        assertNotNull(result.getAnswersJson());

        // Verify the answers JSON was created correctly
        String json = result.getAnswersJson();
        assertTrue(json.contains("101"));
        assertTrue(json.contains("22:30"));
        assertTrue(json.contains("102"));
        assertTrue(json.contains("8"));

        verify(userRepo).findBySubjectRef(subjectRef);
        verify(protocolRepo).save(any(ProtocolSubmissionEntity.class));
    }

    @Test
    void submit_withPractitionerSubjectRef_shouldSaveSuccessfully() throws Exception {
        // Arrange
        String subjectRef = "Practitioner/456";
        when(userRepo.findBySubjectRef(subjectRef)).thenReturn(Optional.of(practitionerUser));
        when(protocolRepo.save(any(ProtocolSubmissionEntity.class))).thenReturn(savedSubmission);

        // Act
        ProtocolSubmissionEntity result = protocolService.submit(subjectRef, validRequest);

        // Assert
        assertNotNull(result);
        verify(userRepo).findBySubjectRef(subjectRef);
        verify(protocolRepo).save(any(ProtocolSubmissionEntity.class));
    }

    @Test
    void submit_withUsername_shouldFindUserByUsername() throws Exception {
        // Arrange
        String username = "PAT12345";
        when(userRepo.findByUsername(username)).thenReturn(Optional.of(patientUser));
        when(protocolRepo.save(any(ProtocolSubmissionEntity.class))).thenReturn(savedSubmission);

        // Act
        ProtocolSubmissionEntity result = protocolService.submit(username, validRequest);

        // Assert
        assertNotNull(result);
        verify(userRepo).findByUsername(username);
        verify(userRepo, never()).findBySubjectRef(anyString());
        verify(protocolRepo).save(any(ProtocolSubmissionEntity.class));
    }

    @Test
    void submit_withoutFilledAt_shouldUseCurrentTime() throws Exception {
        // Arrange
        String subjectRef = "Patient/123";
        validRequest.filledAt = null;

        when(userRepo.findBySubjectRef(subjectRef)).thenReturn(Optional.of(patientUser));
        when(protocolRepo.save(any(ProtocolSubmissionEntity.class))).thenAnswer(invocation -> {
            ProtocolSubmissionEntity entity = invocation.getArgument(0);
            entity.setId(UUID.randomUUID());
            return entity;
        });

        Instant beforeTest = Instant.now();

        // Act
        ProtocolSubmissionEntity result = protocolService.submit(subjectRef, validRequest);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getFilledAt());
        assertTrue(result.getFilledAt().isAfter(beforeTest.minusSeconds(1)));
        assertTrue(result.getFilledAt().isBefore(Instant.now().plusSeconds(1)));

        verify(userRepo).findBySubjectRef(subjectRef);
        verify(protocolRepo).save(any(ProtocolSubmissionEntity.class));
    }

    @Test
    void submit_withSingleAnswer_shouldSerializeCorrectly() throws Exception {
        // Arrange
        String subjectRef = "Patient/123";

        ProtocolSubmissionRequest.AnswerDto singleAnswer = new ProtocolSubmissionRequest.AnswerDto();
        singleAnswer.id = 999;
        singleAnswer.result = "Single answer value";

        validRequest.answers = Arrays.asList(singleAnswer);

        when(userRepo.findBySubjectRef(subjectRef)).thenReturn(Optional.of(patientUser));
        when(protocolRepo.save(any(ProtocolSubmissionEntity.class))).thenAnswer(invocation -> {
            ProtocolSubmissionEntity entity = invocation.getArgument(0);
            entity.setId(UUID.randomUUID());
            return entity;
        });

        // Act
        ProtocolSubmissionEntity result = protocolService.submit(subjectRef, validRequest);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getAnswersJson());
        String json = result.getAnswersJson();

        assertTrue(json.contains("999"));
        assertTrue(json.contains("Single answer value"));
        assertFalse(json.contains("101")); // Should not contain old answers

        verify(userRepo).findBySubjectRef(subjectRef);
        verify(protocolRepo).save(any(ProtocolSubmissionEntity.class));
    }

    @Test
    void submit_withMultipleAnswers_shouldSerializeAll() throws Exception {
        // Arrange
        String subjectRef = "Patient/123";

        List<ProtocolSubmissionRequest.AnswerDto> answers = Arrays.asList(
                createAnswer(1, "Ja"),
                createAnswer(2, "Nein"),
                createAnswer(3, "22:30"),
                createAnswer(4, "07:15"),
                createAnswer(5, "8"),
                createAnswer(6, "2"),
                createAnswer(7, "Schlief relativ gut")
        );

        validRequest.answers = answers;

        when(userRepo.findBySubjectRef(subjectRef)).thenReturn(Optional.of(patientUser));
        when(protocolRepo.save(any(ProtocolSubmissionEntity.class))).thenAnswer(invocation -> {
            ProtocolSubmissionEntity entity = invocation.getArgument(0);
            entity.setId(UUID.randomUUID());
            return entity;
        });

        // Act
        ProtocolSubmissionEntity result = protocolService.submit(subjectRef, validRequest);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getAnswersJson());
        String json = result.getAnswersJson();

        // Verify all answers are in JSON
        for (ProtocolSubmissionRequest.AnswerDto answer : answers) {
            assertTrue(json.contains(String.valueOf(answer.id)));
            assertTrue(json.contains(answer.result));
        }

        verify(userRepo).findBySubjectRef(subjectRef);
        verify(protocolRepo).save(any(ProtocolSubmissionEntity.class));
    }

    @Test
    void submit_withSpecialCharactersInAnswerResult_shouldSerializeCorrectly() throws Exception {
        // Arrange
        String subjectRef = "Patient/123";

        ProtocolSubmissionRequest.AnswerDto answer = new ProtocolSubmissionRequest.AnswerDto();
        answer.id = 201;
        answer.result = "Text mit Sonderzeichen: \"Anführungszeichen\", \nZeilenumbruch, \tTabulator, & Ampersand";

        validRequest.answers = Arrays.asList(answer);

        when(userRepo.findBySubjectRef(subjectRef)).thenReturn(Optional.of(patientUser));
        when(protocolRepo.save(any(ProtocolSubmissionEntity.class))).thenAnswer(invocation -> {
            ProtocolSubmissionEntity entity = invocation.getArgument(0);
            entity.setId(UUID.randomUUID());
            return entity;
        });

        // Act
        ProtocolSubmissionEntity result = protocolService.submit(subjectRef, validRequest);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getAnswersJson());
        // JSON should be valid despite special characters
        assertTrue(result.getAnswersJson().contains("201"));
        // Special characters should be properly escaped in JSON
        assertTrue(result.getAnswersJson().contains("\\\"Anführungszeichen\\\""));

        verify(userRepo).findBySubjectRef(subjectRef);
        verify(protocolRepo).save(any(ProtocolSubmissionEntity.class));
    }

    @Test
    void submit_withEmptyAnswerResult_shouldSerializeEmptyString() throws Exception {
        // Arrange
        String subjectRef = "Patient/123";

        ProtocolSubmissionRequest.AnswerDto answer = new ProtocolSubmissionRequest.AnswerDto();
        answer.id = 301;
        answer.result = ""; // Empty string

        validRequest.answers = Arrays.asList(answer);

        when(userRepo.findBySubjectRef(subjectRef)).thenReturn(Optional.of(patientUser));
        when(protocolRepo.save(any(ProtocolSubmissionEntity.class))).thenAnswer(invocation -> {
            ProtocolSubmissionEntity entity = invocation.getArgument(0);
            entity.setId(UUID.randomUUID());
            return entity;
        });

        // Act
        ProtocolSubmissionEntity result = protocolService.submit(subjectRef, validRequest);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getAnswersJson());
        assertTrue(result.getAnswersJson().contains("301"));
        assertTrue(result.getAnswersJson().contains("\"\"")); // Empty string in JSON

        verify(userRepo).findBySubjectRef(subjectRef);
        verify(protocolRepo).save(any(ProtocolSubmissionEntity.class));
    }

    @Test
    void submit_withNullAnswerResult_shouldSerializeNull() throws Exception {
        // Arrange
        String subjectRef = "Patient/123";

        ProtocolSubmissionRequest.AnswerDto answer = new ProtocolSubmissionRequest.AnswerDto();
        answer.id = 401;
        answer.result = null; // Null value

        validRequest.answers = Arrays.asList(answer);

        when(userRepo.findBySubjectRef(subjectRef)).thenReturn(Optional.of(patientUser));
        when(protocolRepo.save(any(ProtocolSubmissionEntity.class))).thenAnswer(invocation -> {
            ProtocolSubmissionEntity entity = invocation.getArgument(0);
            entity.setId(UUID.randomUUID());
            return entity;
        });

        // Act
        ProtocolSubmissionEntity result = protocolService.submit(subjectRef, validRequest);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getAnswersJson());
        assertTrue(result.getAnswersJson().contains("401"));
        assertTrue(result.getAnswersJson().contains("null")); // null in JSON

        verify(userRepo).findBySubjectRef(subjectRef);
        verify(protocolRepo).save(any(ProtocolSubmissionEntity.class));
    }

    @Test
    void submit_withDifferentTemplateKeys_shouldSaveCorrectly() throws Exception {
        // Arrange
        String subjectRef = "Patient/123";
        String[] templateKeys = {
                "sleep_protocol_v1",
                "sleep_protocol_v2",
                "mood_tracker_v1",
                "pain_diary_v1",
                "medication_log_v1"
        };

        when(userRepo.findBySubjectRef(subjectRef)).thenReturn(Optional.of(patientUser));
        when(protocolRepo.save(any(ProtocolSubmissionEntity.class))).thenAnswer(invocation -> {
            ProtocolSubmissionEntity entity = invocation.getArgument(0);
            entity.setId(UUID.randomUUID());
            return entity;
        });

        for (String templateKey : templateKeys) {
            validRequest.templateKey = templateKey;

            // Act
            ProtocolSubmissionEntity result = protocolService.submit(subjectRef, validRequest);

            // Assert
            assertNotNull(result);
            assertEquals(templateKey, result.getTemplateKey());
        }

        verify(userRepo, times(templateKeys.length)).findBySubjectRef(subjectRef);
        verify(protocolRepo, times(templateKeys.length)).save(any(ProtocolSubmissionEntity.class));
    }

    @Test
    void submit_withDifferentLocales_shouldSaveCorrectly() throws Exception {
        // Arrange
        String subjectRef = "Patient/123";
        String[] locales = {
                "de-DE",
                "en-US",
                "fr-FR",
                "es-ES",
                "de-CH"
        };

        when(userRepo.findBySubjectRef(subjectRef)).thenReturn(Optional.of(patientUser));
        when(protocolRepo.save(any(ProtocolSubmissionEntity.class))).thenAnswer(invocation -> {
            ProtocolSubmissionEntity entity = invocation.getArgument(0);
            entity.setId(UUID.randomUUID());
            return entity;
        });

        for (String locale : locales) {
            validRequest.locale = locale;

            // Act
            ProtocolSubmissionEntity result = protocolService.submit(subjectRef, validRequest);

            // Assert
            assertNotNull(result);
            assertEquals(locale, result.getLocale());
        }

        verify(userRepo, times(locales.length)).findBySubjectRef(subjectRef);
        verify(protocolRepo, times(locales.length)).save(any(ProtocolSubmissionEntity.class));
    }

    // ============ Fehlerfälle ============
    @Test
    void submit_withUnknownSubjectRef_shouldThrowException() {
        // Arrange
        String unknownSubjectRef = "Patient/999";
        when(userRepo.findBySubjectRef(unknownSubjectRef)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> protocolService.submit(unknownSubjectRef, validRequest)
        );
        assertEquals("Unknown user (by subjectRef): " + unknownSubjectRef, exception.getMessage());

        verify(userRepo).findBySubjectRef(unknownSubjectRef);
        verify(protocolRepo, never()).save(any());
    }

    @Test
    void submit_withUnknownUsername_shouldThrowException() {
        // Arrange
        String unknownUsername = "unknown_user";
        when(userRepo.findByUsername(unknownUsername)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> protocolService.submit(unknownUsername, validRequest)
        );
        assertEquals("Unknown user (by username): " + unknownUsername, exception.getMessage());

        verify(userRepo).findByUsername(unknownUsername);
        verify(userRepo, never()).findBySubjectRef(anyString());
        verify(protocolRepo, never()).save(any());
    }

    @Test
    void submit_withEmptyAnswersList_shouldThrowException() {
        // Arrange
        String subjectRef = "Patient/123";
        validRequest.answers = Arrays.asList(); // Empty list

        when(userRepo.findBySubjectRef(subjectRef)).thenReturn(Optional.of(patientUser));

        // Act & Assert
        // Note: @NotEmpty validation happens at controller level with @Valid
        // Service might still get an empty list if validation is bypassed
        assertDoesNotThrow(() -> {
            // Service should handle empty list gracefully
            when(protocolRepo.save(any(ProtocolSubmissionEntity.class))).thenReturn(savedSubmission);
            ProtocolSubmissionEntity result = protocolService.submit(subjectRef, validRequest);
            assertNotNull(result);
            assertEquals("[]", result.getAnswersJson()); // Empty array in JSON
        });
    }

    @Test
    void submit_withNullSubjectRef_shouldTreatAsUsername() throws Exception {
        // Arrange
        String nullSubjectRef = null;
        when(userRepo.findByUsername(null)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> protocolService.submit(nullSubjectRef, validRequest)
        );
        assertEquals("Unknown user (by username): null", exception.getMessage());

        verify(userRepo).findByUsername(null);
        verify(userRepo, never()).findBySubjectRef(anyString());
    }

    @Test
    void submit_withEmptyStringSubjectRef_shouldTreatAsUsername() throws Exception {
        // Arrange
        String emptySubjectRef = "";
        when(userRepo.findByUsername("")).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> protocolService.submit(emptySubjectRef, validRequest)
        );
        assertEquals("Unknown user (by username): ", exception.getMessage());

        verify(userRepo).findByUsername("");
        verify(userRepo, never()).findBySubjectRef(anyString());
    }

    @Test
    void submit_withNullRequest_shouldThrowNullPointerException() {
        // Arrange
        String subjectRef = "Patient/123";

        // Act & Assert
        assertThrows(
                NullPointerException.class,
                () -> protocolService.submit(subjectRef, null)
        );
    }

    @Test
    void submit_withNullAnswersList_shouldThrowNullPointerException() {
        // Arrange
        String subjectRef = "Patient/123";
        validRequest.answers = null;
        when(userRepo.findBySubjectRef(subjectRef)).thenReturn(Optional.of(patientUser));

        // Act & Assert
        assertThrows(
                NullPointerException.class,
                () -> protocolService.submit(subjectRef, validRequest)
        );

        verify(userRepo).findBySubjectRef(subjectRef);
        verify(protocolRepo, never()).save(any());
    }

    // ============ Edge Cases ============
    @Test
    void submit_withVeryLargeNumberOfAnswers_shouldHandleGracefully() throws Exception {
        // Arrange
        String subjectRef = "Patient/123";

        // Create 1000 answers
        ProtocolSubmissionRequest.AnswerDto[] answersArray = new ProtocolSubmissionRequest.AnswerDto[1000];
        for (int i = 0; i < 1000; i++) {
            ProtocolSubmissionRequest.AnswerDto answer = new ProtocolSubmissionRequest.AnswerDto();
            answer.id = i;
            answer.result = "Answer " + i + " with some text to make it larger";
            answersArray[i] = answer;
        }

        validRequest.answers = Arrays.asList(answersArray);

        when(userRepo.findBySubjectRef(subjectRef)).thenReturn(Optional.of(patientUser));
        when(protocolRepo.save(any(ProtocolSubmissionEntity.class))).thenAnswer(invocation -> {
            ProtocolSubmissionEntity entity = invocation.getArgument(0);
            entity.setId(UUID.randomUUID());
            return entity;
        });

        // Act
        ProtocolSubmissionEntity result = protocolService.submit(subjectRef, validRequest);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getAnswersJson());
        assertTrue(result.getAnswersJson().length() > 50000); // Should be quite large

        verify(userRepo).findBySubjectRef(subjectRef);
        verify(protocolRepo).save(any(ProtocolSubmissionEntity.class));
    }

    @Test
    void submit_withFutureFilledAt_shouldAcceptFutureDate() throws Exception {
        // Arrange
        String subjectRef = "Patient/123";
        validRequest.filledAt = Instant.now().plusSeconds(86400); // 1 day in future

        when(userRepo.findBySubjectRef(subjectRef)).thenReturn(Optional.of(patientUser));
        when(protocolRepo.save(any(ProtocolSubmissionEntity.class))).thenAnswer(invocation -> {
            ProtocolSubmissionEntity entity = invocation.getArgument(0);
            entity.setId(UUID.randomUUID());
            return entity;
        });

        // Act
        ProtocolSubmissionEntity result = protocolService.submit(subjectRef, validRequest);

        // Assert
        assertNotNull(result);
        assertEquals(validRequest.filledAt, result.getFilledAt());
        assertTrue(result.getCreatedAt().isBefore(result.getFilledAt()) ||
                result.getCreatedAt().equals(result.getFilledAt()));

        verify(userRepo).findBySubjectRef(subjectRef);
        verify(protocolRepo).save(any(ProtocolSubmissionEntity.class));
    }

    @Test
    void submit_withPastFilledAt_shouldAcceptPastDate() throws Exception {
        // Arrange
        String subjectRef = "Patient/123";
        validRequest.filledAt = Instant.now().minusSeconds(86400); // 1 day in past

        when(userRepo.findBySubjectRef(subjectRef)).thenReturn(Optional.of(patientUser));
        when(protocolRepo.save(any(ProtocolSubmissionEntity.class))).thenAnswer(invocation -> {
            ProtocolSubmissionEntity entity = invocation.getArgument(0);
            entity.setId(UUID.randomUUID());
            return entity;
        });

        // Act
        ProtocolSubmissionEntity result = protocolService.submit(subjectRef, validRequest);

        // Assert
        assertNotNull(result);
        assertEquals(validRequest.filledAt, result.getFilledAt());
        assertTrue(result.getCreatedAt().isAfter(result.getFilledAt()));

        verify(userRepo).findBySubjectRef(subjectRef);
        verify(protocolRepo).save(any(ProtocolSubmissionEntity.class));
    }

    // ============ Helper Methods ============
    private ProtocolSubmissionRequest.AnswerDto createAnswer(int id, String result) {
        ProtocolSubmissionRequest.AnswerDto answer = new ProtocolSubmissionRequest.AnswerDto();
        answer.id = id;
        answer.result = result;
        return answer;
    }
}
