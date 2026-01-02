package de.schlaftagebuch.fhir.provider;

import ca.uhn.fhir.rest.annotation.Create;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.server.IResourceProvider;
import de.schlaftagebuch.fhir.store.ConsentStore;
import org.hl7.fhir.r4.model.Consent;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.ResourceType;
import org.springframework.stereotype.Component;
import ca.uhn.fhir.rest.annotation.ResourceParam;

import java.util.UUID;

/**
 * HAPI-FHIR-ResourceProvider für Consent-Ressourcen (Einwilligungen).
 */

@Component
public class ConsentProvider implements IResourceProvider {

    private final ConsentStore store;

    public ConsentProvider(ConsentStore store) {
        this.store = store;
    }

    @Create
    public MethodOutcome create(@ResourceParam Consent consent) {
        String id = UUID.randomUUID().toString();
        consent.setId(new IdType(ResourceType.Consent.name(), id));
        store.save(id, consent);

        MethodOutcome out = new MethodOutcome();
        out.setId(new IdType("Consent", id));
        out.setResource(consent);
        out.setCreated(true);
        return out;
    }

    @Override
    public Class<Consent> getResourceType() {
        return Consent.class;
    }
}
