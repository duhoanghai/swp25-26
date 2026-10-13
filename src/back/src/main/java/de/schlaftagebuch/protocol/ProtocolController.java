package de.schlaftagebuch.protocol;

import de.schlaftagebuch.protocol.dto.ProtocolSubmissionRequest;
import de.schlaftagebuch.protocol.dto.ProtocolSubmissionResponse;
import de.schlaftagebuch.protocol.model.ProtocolSubmissionEntity;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * REST-Controller für die Entgegennahme und Verarbeitung
 * von ausgefüllten Protokollen (z. B. Schlafprotokollen).
 */

@RestController
@RequestMapping("/api/protocols")
public class ProtocolController {

    private final ProtocolService service;

    public ProtocolController(ProtocolService service) {
        this.service = service;
    }

    // Patient schickt ausgefülltes Protokoll
    @PostMapping("/submissions")
    public ProtocolSubmissionResponse submit(Authentication auth,
                                             @Valid @RequestBody ProtocolSubmissionRequest req) throws Exception {
        String subjectRef = auth.getName(); // "Patient/..."
        ProtocolSubmissionEntity saved = service.submit(subjectRef, req);
        return ProtocolSubmissionResponse.fromEntity(saved);

    }
}