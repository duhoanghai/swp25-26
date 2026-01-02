package de.schlaftagebuch.dto.protocol;

import java.time.Instant;
import java.util.UUID;

public class ProtocolSubmissionResponse {
    public UUID submissionId;
    public String templateKey;
    public String locale;
    public Instant filledAt;
    public Instant storedAt;

    public static ProtocolSubmissionResponse fromEntity(de.schlaftagebuch.model.ProtocolSubmissionEntity e) {
        ProtocolSubmissionResponse r = new ProtocolSubmissionResponse();
        r.submissionId = e.getId();
        r.templateKey = e.getTemplateKey();
        r.locale = e.getLocale();
        r.filledAt = e.getFilledAt();
        r.storedAt = e.getCreatedAt();
        return r;
    }
}
