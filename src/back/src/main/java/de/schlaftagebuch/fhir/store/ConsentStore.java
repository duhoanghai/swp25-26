package de.schlaftagebuch.fhir.store;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import de.schlaftagebuch.auth.model.ConsentEntity;
import de.schlaftagebuch.auth.repository.ConsentRepository;
import org.hl7.fhir.r4.model.Consent;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

// FHIR ConsentStore mit Memory Cache und DB-Persistierung.

@Service
public class ConsentStore {

    private final Map<String, Consent> consents = new ConcurrentHashMap<>();
    private final ConsentRepository consentRepo;
    private final FhirContext fhirContext;
    private final IParser jsonParser;

    public ConsentStore(ConsentRepository consentRepo, FhirContext fhirContext) {
        this.consentRepo = consentRepo;
        this.fhirContext = fhirContext;
        this.jsonParser = fhirContext.newJsonParser();
        loadFromDatabase();
    }

    public Consent save(String id, Consent consent) {
        consents.put(id, consent);
        saveToDatabase(id, consent);
        return consent;
    }

    public Optional<Consent> findById(String id) {
        return Optional.ofNullable(consents.get(id));
    }

    private void loadFromDatabase() {
        consentRepo.findAll().forEach(entity -> {
            try {
                Consent consent = (Consent) jsonParser.parseResource(entity.getFhirJson());
                consents.put(entity.getConsentId(), consent);
            } catch (Exception ignored) {}
        });
    }

    private void saveToDatabase(String consentId, Consent consent) {
        ConsentEntity entity = consentRepo.findByConsentId(consentId)
                .orElseGet(() -> {
                    ConsentEntity newEntity = new ConsentEntity();
                    newEntity.setConsentId(consentId);
                    return newEntity;
                });

        String patientRef = consent.hasPatient() ? consent.getPatient().getReference() : null;
        if (patientRef != null && patientRef.startsWith("Patient/")) {
            entity.setPatientId(patientRef.substring("Patient/".length()));
        }

        entity.setStatus(consent.getStatus() != null ? consent.getStatus().toCode() : "unknown");
        entity.setFhirJson(jsonParser.encodeResourceToString(consent));

        consentRepo.save(entity);
    }
}
