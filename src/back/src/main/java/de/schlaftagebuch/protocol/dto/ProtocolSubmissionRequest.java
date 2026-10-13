package de.schlaftagebuch.protocol.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.time.Instant;
import java.util.List;

/**
 * Request-DTO zur Übermittlung eines ausgefüllten Protokolls
 * vom Frontend an das Systembackend.
 *
 * Das Objekt enthält Metadaten zum verwendeten Protokolltemplate
 * sowie die vom Nutzer eingegebenen Antworten.
 */


public class ProtocolSubmissionRequest {

    @NotBlank
    public String templateKey;   // "prototype_1_v1"

    @NotBlank
    public String locale;        // DE

    public Instant filledAt;     // Ist kein Wert gesetzt, wird im Backend automatisch der aktuelle Zeitpunkt verwendet.

    @NotEmpty
    public List<AnswerDto> answers;

    public static class AnswerDto {
        public int id;
        public String result;
    }
}
