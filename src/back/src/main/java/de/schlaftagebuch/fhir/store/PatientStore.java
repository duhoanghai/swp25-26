package de.schlaftagebuch.fhir.store;

import de.schlaftagebuch.auth.dto.PatientEntity;
import de.schlaftagebuch.auth.repository.PatientRepository;
import org.hl7.fhir.r4.model.HumanName;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Patient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * FHIR Patient Store mit Memory Cache und DB-Persistierung.
 *
 * Beim Server-Start werden alle Patienten aus der Datenbank in den Cache geladen.
 * Jede Änderung wird automatisch sowohl im Cache als auch in der DB gespeichert.
 *
 * KEINE DB-Synchronisation nötig (das macht AuthService selbst).
 */
@Service
public class PatientStore {

    private static final Logger log = LoggerFactory.getLogger(PatientStore.class);

    private final Map<String, Patient> patients = new ConcurrentHashMap<>();
    private final PatientRepository patientRepo;

    public PatientStore(PatientRepository patientRepo) {
        this.patientRepo = patientRepo;
        loadFromDatabase();
    }

    public Patient save(String id, Patient patient) {
        patients.put(id, patient);
        return patient;
    }

    /**
     * Gibt alle gespeicherten Patienten zurück.
     * Wird vom ArztManagementService benötigt.
     */
    public Map<String, Patient> findAll() {
        return Map.copyOf(patients);
    }

    public Optional<Patient> findById(String id) {
        return Optional.ofNullable(patients.get(id));
    }

    /**
     * Lädt beim Server-Start alle Patienten aus der Datenbank in den Cache.
     */
    private void loadFromDatabase() {
        log.info("Lade Patienten aus Datenbank...");
        int count = 0;

        for (PatientEntity entity : patientRepo.findAll()) {
            Patient fhir = toFhir(entity);
            patients.put(entity.getPatientId(), fhir);
            count++;
        }

        log.info("{} Patienten aus DB geladen", count);
    }

    /**
     * Konvertiert eine PatientEntity in einen FHIR Patient.
     */
    private Patient toFhir(PatientEntity entity) {
        Patient p = new Patient();
        p.setId(new IdType("Patient", entity.getPatientId()));

        HumanName name = new HumanName();
        if (entity.getFamilyName() != null) {
            name.setFamily(entity.getFamilyName());
        }
        if (entity.getGivenName() != null) {
            name.addGiven(entity.getGivenName());
        }
        p.addName(name);

        if (entity.getBirthDate() != null) {
            p.setBirthDate(java.sql.Date.valueOf(entity.getBirthDate()));
        }

        return p;
    }
}