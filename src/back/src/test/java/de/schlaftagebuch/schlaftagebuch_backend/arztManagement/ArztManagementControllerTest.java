package de.schlaftagebuch.schlaftagebuch_backend.arztManagement;

import de.schlaftagebuch.arztManagement.ArztDashboardService;
import de.schlaftagebuch.arztManagement.ArztManagementController;
import de.schlaftagebuch.arztManagement.ArztManagementService;
import de.schlaftagebuch.arztManagement.dto.CodeInfoResponse;
import de.schlaftagebuch.arztManagement.dto.PatientSummaryResponse;
import de.schlaftagebuch.arztManagement.dto.DashboardStatsResponse;
import de.schlaftagebuch.arztManagement.dto.PatientDetailResponse;
import de.schlaftagebuch.arztManagement.dto.PatientReportRowResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ArztManagementControllerTest {

    @Mock
    private ArztManagementService arztManagementService;

    @Mock
    private ArztDashboardService arztDashboardService;

    @InjectMocks
    private ArztManagementController controller;

    private PatientSummaryResponse mockPatientSummary;
    private DashboardStatsResponse mockStats;
    private PatientReportRowResponse mockReportRow;
    private PatientDetailResponse mockPatientDetail;
    private CodeInfoResponse mockCodeInfo;

    @BeforeEach
    void setUp() {
        // Mock Patient Summary
        mockPatientSummary = new PatientSummaryResponse();
        mockPatientSummary.setPatientId("123");
        mockPatientSummary.setDisplayName("Mustermann, Max");

        // Mock Stats
        mockStats = new DashboardStatsResponse();
        mockStats.patientsCount = 5L;
        mockStats.reportsCount = 20L;

        // Mock Report Row
        mockReportRow = new PatientReportRowResponse();
        mockReportRow.patientId = "123";

        // Mock Patient Detail
        mockPatientDetail = new PatientDetailResponse();
        mockPatientDetail.patientId = "123";

        // Mock Code Info
        mockCodeInfo = new CodeInfoResponse();
        mockCodeInfo.id = UUID.randomUUID();
        mockCodeInfo.status = "ACTIVE";
    }

    @Test
    void listPatients_shouldCallService() {
        // Arrange
        when(arztManagementService.listPatients())
                .thenReturn(List.of(mockPatientSummary));

        // Act
        List<PatientSummaryResponse> result = controller.listPatients();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("123", result.get(0).getPatientId());
        verify(arztManagementService).listPatients();
    }

    @Test
    void stats_shouldCallDashboardService() {
        // Arrange
        when(arztDashboardService.getStats())
                .thenReturn(mockStats);

        // Act
        DashboardStatsResponse result = controller.stats();

        // Assert
        assertNotNull(result);
        assertEquals(5L, result.patientsCount);
        verify(arztDashboardService).getStats();
    }

    @Test
    void patientReports_shouldCallDashboardService() {
        // Arrange
        when(arztDashboardService.listPatientReportRows())
                .thenReturn(List.of(mockReportRow));

        // Act
        List<PatientReportRowResponse> result = controller.patientReports();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("123", result.get(0).patientId);
        verify(arztDashboardService).listPatientReportRows();
    }

    @Test
    void patientDetails_shouldCallDashboardService() {
        // Arrange
        String patientId = "123";
        when(arztDashboardService.getPatientDetails(patientId))
                .thenReturn(mockPatientDetail);

        // Act
        PatientDetailResponse result = controller.patientDetails(patientId);

        // Assert
        assertNotNull(result);
        assertEquals(patientId, result.patientId);
        verify(arztDashboardService).getPatientDetails(patientId);
    }

    @Test
    void listCodes_shouldCallService() {
        // Arrange
        when(arztManagementService.listCodes())
                .thenReturn(List.of(mockCodeInfo));

        // Act
        List<CodeInfoResponse> result = controller.listCodes();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("ACTIVE", result.get(0).status);
        verify(arztManagementService).listCodes();
    }

    @Test
    void deactivate_shouldCallService() {
        // Arrange
        UUID codeId = mockCodeInfo.id;
        when(arztManagementService.deactivateCode(codeId))
                .thenReturn(mockCodeInfo);

        // Act
        CodeInfoResponse result = controller.deactivate(codeId);

        // Assert
        assertNotNull(result);
        assertEquals(codeId, result.id);
        verify(arztManagementService).deactivateCode(codeId);
    }

    @Test
    void listPatients_emptyList_shouldReturnEmptyList() {
        // Arrange
        when(arztManagementService.listPatients())
                .thenReturn(Collections.emptyList());

        // Act
        List<PatientSummaryResponse> result = controller.listPatients();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void listCodes_emptyList_shouldReturnEmptyList() {
        // Arrange
        when(arztManagementService.listCodes())
                .thenReturn(Collections.emptyList());

        // Act
        List<CodeInfoResponse> result = controller.listCodes();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}
