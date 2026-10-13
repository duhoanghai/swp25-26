package de.schlaftagebuch.protocol;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.schlaftagebuch.protocol.dto.ProtocolSubmissionRequest;
import de.schlaftagebuch.protocol.model.ProtocolSubmissionEntity;
import de.schlaftagebuch.auth.model.UserAccountEntity;
import de.schlaftagebuch.protocol.repository.ProtocolSubmissionRepository;
import de.schlaftagebuch.auth.repository.UserAccountRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;

/**
 * Service-Klasse für die Verarbeitung und Speicherung
 * von ausgefüllten Protokollen (z. B. Schlafprotokolle).
 *
 * Diese Klasse enthält die Geschäftslogik für das
 * Absenden eines Protokolls durch einen authentifizierten Benutzer.
 */

@Service
public class ProtocolService {

    private final UserAccountRepository userRepo;
    private final ProtocolSubmissionRepository repo;
    private final ObjectMapper om = new ObjectMapper();

    // ObjectMapper zur Serialisierung der Antworten in JSON
    public ProtocolService(UserAccountRepository userRepo, ProtocolSubmissionRepository repo) {
        this.userRepo = userRepo;
        this.repo = repo;
    }

    /**
     * Speichert ein ausgefülltes Protokoll in der Datenbank.
     *
     * @param authName Name des authentifizierten Subjekts.
     *                 Kann entweder ein subjectRef
     *                 (z. B. "Patient/123" oder "Practitioner/5")
     *                 oder ein Username sein.
     * @param req      Übertragene Protokolldaten
     * @return gespeichertes ProtocolSubmissionEntity
     * @throws Exception bei Fehlern während der JSON-Serialisierung
     */

    public ProtocolSubmissionEntity submit(String authName, ProtocolSubmissionRequest req) throws Exception {

        UserAccountEntity user;

        // Falls authName wie ein FHIR-subjectRef aussieht, wird der Benutzer über subjectRef gesucht
        if (authName != null && (authName.startsWith("Patient/") || authName.startsWith("Practitioner/"))) {
            user = userRepo.findBySubjectRef(authName)
                    .orElseThrow(() -> new IllegalArgumentException("Unknown user (by subjectRef): " + authName));
        } else {
            // Andernfalls wird authName als Username interpretiert
            user = userRepo.findByUsername(authName)
                    .orElseThrow(() -> new IllegalArgumentException("Unknown user (by username): " + authName));
        }

        String subjectRef = authName; // "Patient/..."
        // optional: Falls ausschließlich subjectRefs verwendet werden kann hier eine zusätzliche Validierung erfolgen
        // userRepo.findBySubjectRef(subjectRef).orElseThrow(() -> new NotFoundException("Unknown patient"));

        // Neues Protokoll-Entity erzeugen
        ProtocolSubmissionEntity e = new ProtocolSubmissionEntity();
        e.setSubjectRef(subjectRef);
        e.setTemplateKey(req.templateKey);
        e.setLocale(req.locale);
        e.setFilledAt(req.filledAt != null ? req.filledAt : Instant.now());
        e.setCreatedAt(Instant.now());
        e.setAnswersJson(om.writeValueAsString(req.answers));

        return repo.save(e);
    }
}
