package de.schlaftagebuch.controller;

import de.schlaftagebuch.dto.protocol.ProtocolSubmissionRequest;
import de.schlaftagebuch.dto.protocol.ProtocolSubmissionResponse;
import de.schlaftagebuch.model.ProtocolSubmissionEntity;
import de.schlaftagebuch.repository_database.ProtocolSubmissionRepository;
import de.schlaftagebuch.service.ProtocolService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/protocols")
public class ProtocolController {

    private final ProtocolService service;
    private final ProtocolSubmissionRepository repo;

    public ProtocolController(ProtocolService service, ProtocolSubmissionRepository repo) {
        this.service = service;
        this.repo = repo;
    }

    // ✅ пациент отправляет заполненный протокол
    @PostMapping("/submissions")
    public ProtocolSubmissionResponse submit(Authentication auth,
                                             @Valid @RequestBody ProtocolSubmissionRequest req) throws Exception {
        String subjectRef = auth.getName(); // "Patient/..."
        ProtocolSubmissionEntity saved = service.submit(subjectRef, req);
        return ProtocolSubmissionResponse.fromEntity(saved);

    }

    // ✅ пациент получает свои протоколы
    @GetMapping("/submissions/mine")
    public List<ProtocolSubmissionResponse> mine(Authentication auth) {
        String subjectRef = auth.getName(); // "Patient/..."
        return repo.findAllBySubjectRefOrderByFilledAtAsc(subjectRef)
                .stream()
                .map(ProtocolSubmissionResponse::fromEntity)
                .toList();
    }
}
