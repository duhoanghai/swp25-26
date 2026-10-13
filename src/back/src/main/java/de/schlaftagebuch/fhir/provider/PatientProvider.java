package de.schlaftagebuch.fhir.provider;

import ca.uhn.fhir.rest.annotation.Create;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.annotation.Update;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.server.IResourceProvider;
import de.schlaftagebuch.fhir.store.PatientStore;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.ResourceType;
import org.springframework.stereotype.Component;
import ca.uhn.fhir.rest.annotation.ResourceParam;

import java.util.UUID;

/**
 * HAPI-FHIR-ResourceProvider für Patient-Ressourcen.
 * Stellt CRUD-Operationen über /fhir/Patient zur Verfügung und nutzt intern den PatientStore.
 */

@Component
public class PatientProvider implements IResourceProvider {

    private final PatientStore store;

    public PatientProvider(PatientStore store) {
        this.store = store;
    }

    @Create
    public MethodOutcome create(@ResourceParam Patient patient) {
        String id = UUID.randomUUID().toString();

        patient.setId(new IdType(ResourceType.Patient.name(), id));

        store.save(id, patient);

        MethodOutcome out = new MethodOutcome();
        out.setId(new IdType("Patient", id));
        out.setResource(patient);
        out.setCreated(true);
        return out;
    }

    @Read
    public Patient read(@IdParam IdType id) {
        String patientId = id.getIdPart();
        return store.findById(patientId)
                .orElseThrow(() -> new IllegalArgumentException("Patient not found: " + patientId));
    }

    @Update
    public MethodOutcome update(@IdParam IdType id, @ResourceParam Patient patient) {
        String patientId = id.getIdPart();

        patient.setId(new IdType("Patient", patientId));
        store.save(patientId, patient);

        MethodOutcome out = new MethodOutcome();
        out.setId(new IdType("Patient", patientId));
        out.setResource(patient);
        out.setCreated(false);
        return out;
    }

    @Override
    public Class<Patient> getResourceType() {
        return Patient.class;
    }
}
