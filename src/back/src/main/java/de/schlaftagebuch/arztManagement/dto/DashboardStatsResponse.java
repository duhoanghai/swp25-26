package de.schlaftagebuch.arztManagement.dto;

import java.time.Instant;
import java.util.List;

/**
 * DTO für das Arzt-Dashboard (Zahlen + letzte Aktivitäten).
 */
public class DashboardStatsResponse {
    public long patientsCount;
    public long reportsCount;
    public long newReportsLast7Days;
    public List<RecentReport> recentReports;

    public static class RecentReport {
        public String patientRef;     // z.B. "Patient/..."
        public String patientDisplay; // z.B. "... (Login: ...)"
        public Instant createdAt;
        public String reportType;
    }
}
