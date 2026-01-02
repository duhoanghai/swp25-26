package de.schlaftagebuch.fhir.store;

import org.hl7.fhir.r4.model.Patient;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Einfache In-Memory-Verwaltung von FHIR-Patient-Ressourcen.
 * In einer echten Anwendung könnte dies durch eine Datenbank oder einen externen FHIR-Server ersetzt werden.
 */

@Service
public class PatientStore {
    private final Map<String, Patient> patients = new ConcurrentHashMap<>();

    public Patient save(String id, Patient patient) {
        patients.put(id, patient);
        return patient;
    }

    public Optional<Patient> findById(String id) {
        return Optional.ofNullable(patients.get(id));
    }
}
