package de.schlaftagebuch.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.schlaftagebuch.dto.protocol.ProtocolSubmissionRequest;
import de.schlaftagebuch.model.ProtocolSubmissionEntity;
import de.schlaftagebuch.model.UserAccountEntity;
import de.schlaftagebuch.repository_database.ProtocolSubmissionRepository;
import de.schlaftagebuch.repository_database.UserAccountRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class ProtocolService {

    private final UserAccountRepository userRepo;
    private final ProtocolSubmissionRepository repo;
    private final ObjectMapper om = new ObjectMapper();

    public ProtocolService(UserAccountRepository userRepo, ProtocolSubmissionRepository repo) {
        this.userRepo = userRepo;
        this.repo = repo;
    }

    public ProtocolSubmissionEntity submit(String authName, ProtocolSubmissionRequest req) throws Exception {

        UserAccountEntity user;

        // Если authName выглядит как subjectRef, ищем по нему
        if (authName != null && (authName.startsWith("Patient/") || authName.startsWith("Practitioner/"))) {
            user = userRepo.findBySubjectRef(authName)
                    .orElseThrow(() -> new IllegalArgumentException("Unknown user (by subjectRef): " + authName));
        } else {
            // иначе считаем, что это username
            user = userRepo.findByUsername(authName)
                    .orElseThrow(() -> new IllegalArgumentException("Unknown user (by username): " + authName));
        }

        String subjectRef = authName; // это уже "Patient/..."
        // optional (позже): если подключена БД и ведём аккаунты
        // userRepo.findBySubjectRef(subjectRef).orElseThrow(() -> new NotFoundException("Unknown patient"));

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
