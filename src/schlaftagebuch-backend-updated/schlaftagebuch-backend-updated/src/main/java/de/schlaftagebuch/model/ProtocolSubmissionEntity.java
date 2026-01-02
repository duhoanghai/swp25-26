package de.schlaftagebuch.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "protocol_submission")
public class ProtocolSubmissionEntity {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false, length = 120)
    private String subjectRef; // "Patient/..."

    @Column(nullable = false, length = 60)
    private String templateKey;

    @Column(nullable = false, length = 10)
    private String locale;

    @Column(nullable = false)
    private Instant filledAt;

    @Lob
    @Column(nullable = false)
    private String answersJson;

    @Column(nullable = false)
    private Instant createdAt;

    public UUID getId() { return id; }

    public String getSubjectRef() { return subjectRef; }
    public void setSubjectRef(String subjectRef) { this.subjectRef = subjectRef; }

    public String getTemplateKey() { return templateKey; }
    public void setTemplateKey(String templateKey) { this.templateKey = templateKey; }

    public String getLocale() { return locale; }
    public void setLocale(String locale) { this.locale = locale; }

    public Instant getFilledAt() { return filledAt; }
    public void setFilledAt(Instant filledAt) { this.filledAt = filledAt; }

    public String getAnswersJson() { return answersJson; }
    public void setAnswersJson(String answersJson) { this.answersJson = answersJson; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    // getters/setters...
}
