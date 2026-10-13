package de.schlaftagebuch.arztManagement;

import de.schlaftagebuch.arztManagement.dto.DashboardStatsResponse;
import de.schlaftagebuch.arztManagement.dto.PatientDetailResponse;
import de.schlaftagebuch.arztManagement.dto.PatientReportRowResponse;
import de.schlaftagebuch.arztManagement.model.OneTimeCodeEntity;
import de.schlaftagebuch.report.model.ReportEntity;
import de.schlaftagebuch.auth.model.Role;
import de.schlaftagebuch.auth.model.UserAccountEntity;
import de.schlaftagebuch.arztManagement.repository.OneTimeCodeRepository;
import de.schlaftagebuch.report.repository.ReportRepository;
import de.schlaftagebuch.auth.repository.UserAccountRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.Duration;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import de.schlaftagebuch.auth.repository.PatientRepository;

@Service
public class ArztDashboardService {

    private final UserAccountRepository userRepo;
    private final ReportRepository reportRepo;
    private final OneTimeCodeRepository codeRepo;
    private final PatientRepository patientRepo;

    public ArztDashboardService(UserAccountRepository userRepo, ReportRepository reportRepo, OneTimeCodeRepository codeRepo, PatientRepository patientRepo) {
        this.userRepo = userRepo;
        this.reportRepo = reportRepo;
        this.codeRepo = codeRepo;
        this.patientRepo = patientRepo;
    }

    public DashboardStatsResponse getStats() {
        DashboardStatsResponse r = new DashboardStatsResponse();
        r.patientsCount = userRepo.countByRole(Role.PATIENT);
        r.reportsCount = reportRepo.count();

        Instant now = Instant.now();
        r.newReportsLast7Days =
                reportRepo.countByCreatedAtAfter(
                        now.minus(Duration.ofDays(7))
                );


        r.recentReports = reportRepo.findTop5ByOrderByCreatedAtDesc()
                .stream()
                .map(e -> toRecent(e))
                .toList();

        return r;
    }

    public List<PatientReportRowResponse> listPatientReportRows() {
        return userRepo.findAllByRole(Role.PATIENT)
                .stream()
                .map(this::toPatientReportRow)
                .sorted(Comparator.comparing((PatientReportRowResponse x) -> x.latestReportCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
    }

    public PatientDetailResponse getPatientDetails(String patientId) {
        String subjectRef = "Patient/" + patientId;
        UserAccountEntity acc = userRepo.findBySubjectRef(subjectRef)
                .orElseThrow(() -> new IllegalArgumentException("Patient not found: " + patientId));

        PatientDetailResponse d = new PatientDetailResponse();
        d.patientRef = subjectRef;
        d.patientId = patientId;
        d.username = acc.getUsername();
        d.display = toPatientDisplay(acc);

        d.reportsCount = reportRepo.countBySubjectRef(subjectRef);
        Optional<ReportEntity> latestReport = reportRepo.findFirstBySubjectRefOrderByCreatedAtDesc(subjectRef);
        latestReport.ifPresent(r -> d.latestReportCreatedAt = r.getCreatedAt());

        List<OneTimeCodeEntity> codes = codeRepo.findAllByPatientRefOrderByCreatedAtDesc(subjectRef);
        if (!codes.isEmpty()) {
            OneTimeCodeEntity c = codes.get(0);
            d.latestCodeId = c.getId();
            d.latestCode = c.getCode();
            d.latestCodeCreatedAt = c.getCreatedAt();
            d.latestCodeExpiresAt = c.getExpiresAt();
            d.latestCodeUsedAt = c.getUsedAt();
            d.latestCodeRevokedAt = c.getRevokedAt();
            if (c.getAppointmentDate() != null) {
                d.latestAppointmentDate = c.getAppointmentDate().toString();
            }


            Instant now = Instant.now();
            if (c.isRevoked()) d.latestCodeStatus = "REVOKED";
            else if (c.isUsed()) d.latestCodeStatus = "USED";
            else if (c.isExpired(now)) d.latestCodeStatus = "EXPIRED";
            else d.latestCodeStatus = "ACTIVE";
        }

        return d;
    }

    private DashboardStatsResponse.RecentReport toRecent(ReportEntity e) {
        DashboardStatsResponse.RecentReport r = new DashboardStatsResponse.RecentReport();
        r.patientRef = e.getSubjectRef();
        r.createdAt = e.getCreatedAt();
        r.reportType = e.getReportType();

        userRepo.findBySubjectRef(e.getSubjectRef()).ifPresent(acc -> r.patientDisplay = toPatientDisplay(acc));
        if (r.patientDisplay == null) r.patientDisplay = e.getSubjectRef();

        return r;
    }

    private PatientReportRowResponse toPatientReportRow(UserAccountEntity acc) {
        String ref = acc.getSubjectRef();
        String id = ref != null && ref.startsWith("Patient/") ? ref.substring("Patient/".length()) : ref;

        PatientReportRowResponse row = new PatientReportRowResponse();
        row.patientRef = ref;
        row.patientId = id;
        row.display = toPatientDisplay(acc);
        row.reportsCount = reportRepo.countBySubjectRef(ref);

        reportRepo.findFirstBySubjectRefOrderByCreatedAtDesc(ref).ifPresent(r -> {
            row.latestReportCreatedAt = r.getCreatedAt();
            row.latestReportType = r.getReportType();
        });

        return row;
    }

    private String toPatientDisplay(UserAccountEntity acc) {
        String ref = acc.getSubjectRef();
        String id = ref != null && ref.startsWith("Patient/") ? ref.substring("Patient/".length()) : ref;
        if (id == null || id.isBlank()) return "—";

        return patientRepo.findByPatientId(id)
                .map(p -> {
                    String family = p.getFamilyName() != null ? p.getFamilyName().trim() : "";
                    String given  = p.getGivenName() != null ? p.getGivenName().trim() : "";
                    String combo = (family + (family.isEmpty() || given.isEmpty() ? "" : ", ") + given).trim();
                    return combo.isEmpty() ? "—" : combo;
                })
                .orElse("—");
    }
}
