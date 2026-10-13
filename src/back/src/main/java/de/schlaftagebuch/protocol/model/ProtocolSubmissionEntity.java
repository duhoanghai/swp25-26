package de.schlaftagebuch.protocol.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/**
 * Persistente Entität zur Speicherung einer
 * Protokollübermittlung (z. B. eines Schlafprotokolls).
 *
 * Die Entität bildet eine einzelne Übermittlung
 * eines ausgefüllten Protokolls durch eine Patientin
 * bzw. einen Patienten ab und wird in der Datenbank
 * persistent gespeichert.
 */

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


    @Column(nullable = false, name = "answers_json", columnDefinition = "text")
    private String answersJson;

    @Column(nullable = false)
    private Instant createdAt;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
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

}
