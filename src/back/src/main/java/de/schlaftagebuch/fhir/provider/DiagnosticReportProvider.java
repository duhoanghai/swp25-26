package de.schlaftagebuch.fhir.provider;

import ca.uhn.fhir.rest.annotation.Create;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.annotation.ResourceParam;
import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.server.IResourceProvider;
import de.schlaftagebuch.fhir.store.DiagnosticReportStore;
import org.hl7.fhir.r4.model.DiagnosticReport;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.ResourceType;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * HAPI-FHIR-ResourceProvider für DiagnosticReport-Ressourcen.
 * Stellt Schlaf-Berichte als FHIR-konforme DiagnosticReports bereit.
 */
@Component
public class DiagnosticReportProvider implements IResourceProvider {

    private final DiagnosticReportStore store;

    public DiagnosticReportProvider(DiagnosticReportStore store) {
        this.store = store;
    }

    /**
     * Erstellt einen neuen DiagnosticReport.
     * POST /fhir/DiagnosticReport
     */
    @Create
    public MethodOutcome create(@ResourceParam DiagnosticReport report) {
        String id = UUID.randomUUID().toString();
        report.setId(new IdType(ResourceType.DiagnosticReport.name(), id));
        store.save(id, report);

        MethodOutcome outcome = new MethodOutcome();
        outcome.setId(new IdType("DiagnosticReport", id));
        outcome.setResource(report);
        outcome.setCreated(true);
        return outcome;
    }

    /**
     * Liest einen DiagnosticReport nach ID.
     * GET /fhir/DiagnosticReport/{id}
     */
    @Read
    public DiagnosticReport read(@IdParam IdType id) {
        String reportId = id.getIdPart();
        return store.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("DiagnosticReport not found: " + reportId));
    }

    /**
     * Sucht DiagnosticReports (vorerst ohne Filter).
     * GET /fhir/DiagnosticReport
     */
    @Search
    public List<DiagnosticReport> search() {
        return new ArrayList<>(store.findAll().values());
    }

    @Override
    public Class<DiagnosticReport> getResourceType() {
        return DiagnosticReport.class;
    }
}