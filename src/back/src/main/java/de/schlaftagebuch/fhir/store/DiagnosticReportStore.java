package de.schlaftagebuch.fhir.store;

import org.hl7.fhir.r4.model.DiagnosticReport;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-Memory-Store für FHIR DiagnosticReport-Ressourcen.
 * Speichert generierte Schlaf-Berichte als FHIR-Ressourcen.
 */
@Component
public class DiagnosticReportStore {

    private final Map<String, DiagnosticReport> reports = new ConcurrentHashMap<>();

    /**
     * Speichert einen DiagnosticReport.
     */
    public void save(String id, DiagnosticReport report) {
        reports.put(id, report);
    }

    /**
     * Findet einen DiagnosticReport nach ID.
     */
    public Optional<DiagnosticReport> findById(String id) {
        return Optional.ofNullable(reports.get(id));
    }

    /**
     * Löscht einen DiagnosticReport.
     */
    public void delete(String id) {
        reports.remove(id);
    }

    /**
     * Gibt alle gespeicherten Reports zurück.
     */
    public Map<String, DiagnosticReport> findAll() {
        return Map.copyOf(reports);
    }

    /**
     * Zählt die Anzahl der gespeicherten Reports.
     */
    public int count() {
        return reports.size();
    }
}
