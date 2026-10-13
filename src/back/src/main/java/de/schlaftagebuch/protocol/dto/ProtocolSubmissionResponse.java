package de.schlaftagebuch.protocol.dto;

import de.schlaftagebuch.protocol.model.ProtocolSubmissionEntity;

import java.time.Instant;
import java.util.UUID;

/**
 * Response-DTO zur Rückgabe der Informationen
 * über eine erfolgreich gespeicherte Protokollübermittlung.
 *
 * Das Objekt wird vom Backend an das Frontend
 * zurückgegeben, nachdem ein Protokoll
 * erfolgreich verarbeitet und gespeichert wurde.
 */


public class ProtocolSubmissionResponse {
    public UUID submissionId;
    public String templateKey;
    public String locale;
    public Instant filledAt;
    public Instant storedAt;

    // Response-DTO aus einer Protokoll-Entität.
    public static ProtocolSubmissionResponse fromEntity(ProtocolSubmissionEntity e) {
        ProtocolSubmissionResponse r = new ProtocolSubmissionResponse();
        r.submissionId = e.getId();
        r.templateKey = e.getTemplateKey();
        r.locale = e.getLocale();
        r.filledAt = e.getFilledAt();
        r.storedAt = e.getCreatedAt();
        return r;
    }
}
