package de.schlaftagebuch.fhir.store;

import org.hl7.fhir.r4.model.Consent;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-Memory-Speicher für FHIR-Consent-Ressourcen (Einwilligungen).
 */

@Service
public class ConsentStore {
    private final Map<String, Consent> consents = new ConcurrentHashMap<>();

    public Consent save(String id, Consent consent) {
        consents.put(id, consent);
        return consent;
    }

    public Optional<Consent> findById(String id) {
        return Optional.ofNullable(consents.get(id));
    }
}
