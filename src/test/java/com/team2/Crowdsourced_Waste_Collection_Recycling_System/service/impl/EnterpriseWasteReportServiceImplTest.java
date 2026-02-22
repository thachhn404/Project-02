package com.team2.Crowdsourced_Waste_Collection_Recycling_System.service.impl;

import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.Enterprise;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.WasteReport;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.enums.WasteReportStatus;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.enterprise.EnterpriseRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.waste.WasteReportRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EnterpriseWasteReportServiceImplTest {

    @Mock
    private WasteReportRepository wasteReportRepository;

    @Mock
    private EnterpriseRepository enterpriseRepository;

    @InjectMocks
    private EnterpriseWasteReportServiceImpl service;

    private Enterprise enterprise;
    private WasteReport wasteReport;

    @BeforeEach
    void setUp() {
        enterprise = new Enterprise();
        enterprise.setId(1);
        enterprise.setSupportedWasteTypeCodes("PLASTIC,PAPER");
        enterprise.setServiceWards("Ward 1,Ward 2");
        enterprise.setServiceCities("City A");

        wasteReport = new WasteReport();
        wasteReport.setId(100);
        wasteReport.setStatus(WasteReportStatus.PENDING);
        wasteReport.setWasteType("PLASTIC");
        wasteReport.setAddress("123 Street, Ward 1, City A");
    }

    @Test
    void acceptReport_Success() {
        when(enterpriseRepository.findById(1)).thenReturn(Optional.of(enterprise));
        when(wasteReportRepository.findById(100)).thenReturn(Optional.of(wasteReport));

        service.acceptReport(1, 100);

        assertEquals(WasteReportStatus.ACCEPTED_ENTERPRISE, wasteReport.getStatus());
        assertEquals(enterprise, wasteReport.getEnterprise());
        assertNotNull(wasteReport.getAcceptedAt());
        verify(wasteReportRepository).save(wasteReport);
    }

    @Test
    void acceptReport_EnterpriseNotFound() {
        when(enterpriseRepository.findById(1)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> service.acceptReport(1, 100));
    }

    @Test
    void acceptReport_ReportNotFound() {
        when(enterpriseRepository.findById(1)).thenReturn(Optional.of(enterprise));
        when(wasteReportRepository.findById(100)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> service.acceptReport(1, 100));
    }

    @Test
    void acceptReport_NotPending() {
        wasteReport.setStatus(WasteReportStatus.ASSIGNED);
        when(enterpriseRepository.findById(1)).thenReturn(Optional.of(enterprise));
        when(wasteReportRepository.findById(100)).thenReturn(Optional.of(wasteReport));

        assertThrows(ResponseStatusException.class, () -> service.acceptReport(1, 100));
    }

    @Test
    void acceptReport_UnsupportedWasteType() {
        wasteReport.setWasteType("METAL");
        when(enterpriseRepository.findById(1)).thenReturn(Optional.of(enterprise));
        when(wasteReportRepository.findById(100)).thenReturn(Optional.of(wasteReport));

        assertThrows(ResponseStatusException.class, () -> service.acceptReport(1, 100));
    }

    @Test
    void acceptReport_NotInServiceArea() {
        wasteReport.setAddress("456 Street, Ward 3, City A");
        when(enterpriseRepository.findById(1)).thenReturn(Optional.of(enterprise));
        when(wasteReportRepository.findById(100)).thenReturn(Optional.of(wasteReport));

        assertThrows(ResponseStatusException.class, () -> service.acceptReport(1, 100));
    }

    @Test
    void rejectReport_Success() {
        when(enterpriseRepository.findById(1)).thenReturn(Optional.of(enterprise));
        when(wasteReportRepository.findById(100)).thenReturn(Optional.of(wasteReport));

        service.rejectReport(1, 100, "Cannot handle");

        assertEquals(WasteReportStatus.REJECTED, wasteReport.getStatus());
        assertEquals(enterprise, wasteReport.getEnterprise());
        assertEquals("Cannot handle", wasteReport.getRejectionReason());
        verify(wasteReportRepository).save(wasteReport);
    }

    @Test
    void rejectReport_NotPending() {
        wasteReport.setStatus(WasteReportStatus.ACCEPTED_ENTERPRISE);
        when(enterpriseRepository.findById(1)).thenReturn(Optional.of(enterprise));
        when(wasteReportRepository.findById(100)).thenReturn(Optional.of(wasteReport));

        assertThrows(ResponseStatusException.class, () -> service.rejectReport(1, 100, "Reason"));
    }
}
