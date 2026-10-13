package de.schlaftagebuch.arztManagement;

import de.schlaftagebuch.arztManagement.dto.CodeInfoResponse;
import de.schlaftagebuch.arztManagement.dto.PatientSummaryResponse;
import de.schlaftagebuch.arztManagement.dto.DashboardStatsResponse;
import de.schlaftagebuch.arztManagement.dto.PatientDetailResponse;
import de.schlaftagebuch.arztManagement.dto.PatientReportRowResponse;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Endpunkte für das Arzt-Frontend (Patientenliste, Code-Liste, Code-Deaktivierung).
 */
@RestController
@RequestMapping("/api/arzt")
public class ArztManagementController {

    private final ArztManagementService svc;
    private final ArztDashboardService dash;

    public ArztManagementController(ArztManagementService svc, ArztDashboardService dash) {
        this.svc = svc;
        this.dash = dash;
    }

    @GetMapping("/patients")
    public List<PatientSummaryResponse> listPatients() {
        System.out.println("=== Controller: /api/arzt/patients aufgerufen ===");
        List<PatientSummaryResponse> result = svc.listPatients();
        System.out.println("Rückgabe: " + result.size() + " Patienten");
        return result;
    }

    @GetMapping("/stats")
    public DashboardStatsResponse stats() {
        return dash.getStats();
    }

    @GetMapping("/patient-reports")
    public List<PatientReportRowResponse> patientReports() {
        return dash.listPatientReportRows();
    }

    @GetMapping("/patients/{patientId}/details")
    public PatientDetailResponse patientDetails(@PathVariable String patientId) {
        return dash.getPatientDetails(patientId);
    }

    @GetMapping("/codes")
    public List<CodeInfoResponse> listCodes() {
        return svc.listCodes();
    }

    @PostMapping("/codes/{codeId}/deactivate")
    public CodeInfoResponse deactivate(@PathVariable UUID codeId) {
        return svc.deactivateCode(codeId);
    }
}

