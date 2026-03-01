package com.team2.Crowdsourced_Waste_Collection_Recycling_System.service;

import com.team2.Crowdsourced_Waste_Collection_Recycling_System.config.WorkRuleProperties;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.CollectionRequest;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.CollectionTracking;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.Collector;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.Enterprise;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.WasteReport;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.enums.CollectionRequestStatus;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.enums.CollectorStatus;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.enums.WasteReportStatus;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.collector.CollectionRequestRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.collector.CollectionTrackingRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.collector.CollectorRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.waste.WasteReportRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.service.impl.TaskAutomationServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaskAutomationServiceImplTest {
    @Mock
    private CollectionRequestRepository collectionRequestRepository;
    @Mock
    private CollectorRepository collectorRepository;
    @Mock
    private CollectionTrackingRepository collectionTrackingRepository;
    @Mock
    private WasteReportRepository wasteReportRepository;
    @Mock
    private WorkRuleProperties workRuleProperties;

    @InjectMocks
    private TaskAutomationServiceImpl service;

    @Test
    void checkAssignedTasksTimeout_reassignsToClosestCollectorWithinRadius() {
        when(workRuleProperties.getAcceptTimeoutHours()).thenReturn(4);
        when(workRuleProperties.getSuspendThreshold()).thenReturn(3);
        when(workRuleProperties.getReassignRadiusKm()).thenReturn(10.0);
        when(workRuleProperties.getWorkingStartHour()).thenReturn(0);
        when(workRuleProperties.getWorkingEndHour()).thenReturn(23);

        Enterprise enterprise = new Enterprise();
        enterprise.setId(1);

        WasteReport report = new WasteReport();
        report.setLatitude(new BigDecimal("10.00000000"));
        report.setLongitude(new BigDecimal("20.00000000"));

        Collector current = new Collector();
        current.setId(10);
        current.setViolationCount(0);
        current.setStatus(CollectorStatus.ACTIVE);

        CollectionRequest request = new CollectionRequest();
        request.setId(100);
        request.setRequestCode("REQ-100");
        request.setEnterprise(enterprise);
        request.setReport(report);
        request.setCollector(current);
        request.setStatus(CollectionRequestStatus.ASSIGNED);

        Collector far = new Collector();
        far.setId(12);
        far.setCurrentLatitude(new BigDecimal("10.50000000"));
        far.setCurrentLongitude(new BigDecimal("20.50000000"));

        Collector near = new Collector();
        near.setId(11);
        near.setCurrentLatitude(new BigDecimal("10.01000000"));
        near.setCurrentLongitude(new BigDecimal("20.01000000"));

        when(collectionRequestRepository.findExpiredAssignedTasks(any())).thenReturn(List.of(request));
        when(collectorRepository.findAvailableCollectors(1)).thenReturn(List.of(current, far, near));
        when(collectionRequestRepository.getReferenceById(100)).thenReturn(request);
        when(collectorRepository.getReferenceById(anyInt())).thenAnswer(inv -> {
            Integer id = inv.getArgument(0);
            if (id.equals(current.getId())) return current;
            if (id.equals(near.getId())) return near;
            if (id.equals(far.getId())) return far;
            Collector c = new Collector();
            c.setId(id);
            return c;
        });
        when(collectionRequestRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(collectionTrackingRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(collectorRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.checkAssignedTasksTimeout();

        assertThat(current.getViolationCount()).isEqualTo(1);
        assertThat(current.getStatus()).isEqualTo(CollectorStatus.ACTIVE);
        assertThat(request.getCollector()).isEqualTo(near);
        assertThat(request.getStatus()).isEqualTo(CollectionRequestStatus.ASSIGNED);
        assertThat(request.getAssignedAt()).isNotNull();

        ArgumentCaptor<CollectionTracking> trackingCaptor = ArgumentCaptor.forClass(CollectionTracking.class);
        verify(collectionTrackingRepository, atLeast(2)).save(trackingCaptor.capture());
        List<CollectionTracking> saved = trackingCaptor.getAllValues();
        assertThat(saved).extracting(CollectionTracking::getAction).contains("timeout", "reassigned");
    }

    @Test
    void checkAssignedTasksTimeout_unassignsWhenNoEligibleCollector() {
        when(workRuleProperties.getAcceptTimeoutHours()).thenReturn(4);
        when(workRuleProperties.getSuspendThreshold()).thenReturn(3);
        when(workRuleProperties.getReassignRadiusKm()).thenReturn(10.0);
        when(workRuleProperties.getWorkingStartHour()).thenReturn(0);
        when(workRuleProperties.getWorkingEndHour()).thenReturn(23);

        Enterprise enterprise = new Enterprise();
        enterprise.setId(1);

        WasteReport report = new WasteReport();
        report.setLatitude(new BigDecimal("10.00000000"));
        report.setLongitude(new BigDecimal("20.00000000"));
        report.setStatus(WasteReportStatus.ASSIGNED);

        Collector current = new Collector();
        current.setId(10);
        current.setViolationCount(0);
        current.setStatus(CollectorStatus.ACTIVE);

        CollectionRequest request = new CollectionRequest();
        request.setId(101);
        request.setRequestCode("REQ-101");
        request.setEnterprise(enterprise);
        request.setReport(report);
        request.setCollector(current);
        request.setStatus(CollectionRequestStatus.ASSIGNED);

        when(collectionRequestRepository.findExpiredAssignedTasks(any())).thenReturn(List.of(request));
        when(collectorRepository.findAvailableCollectors(1)).thenReturn(List.of(current));
        when(collectionRequestRepository.getReferenceById(101)).thenReturn(request);
        when(collectorRepository.getReferenceById(10)).thenReturn(current);
        when(collectionRequestRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(collectionTrackingRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(collectorRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(wasteReportRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.checkAssignedTasksTimeout();

        assertThat(request.getCollector()).isNull();
        assertThat(request.getStatus()).isEqualTo(CollectionRequestStatus.ACCEPTED_ENTERPRISE);
        assertThat(request.getAssignedAt()).isNull();
        assertThat(request.getRejectionReason()).isNotBlank();
        assertThat(report.getStatus()).isEqualTo(WasteReportStatus.ACCEPTED_ENTERPRISE);
    }

    @Test
    void checkSlaViolations_marksSlaViolatedAndSuspendsAtThreshold() {
        when(workRuleProperties.getSlaHours()).thenReturn(72);
        when(workRuleProperties.getSuspendThreshold()).thenReturn(3);

        Collector collector = new Collector();
        collector.setId(20);
        collector.setViolationCount(2);
        collector.setStatus(CollectorStatus.ACTIVE);

        CollectionRequest request = new CollectionRequest();
        request.setId(201);
        request.setRequestCode("REQ-201");
        request.setCollector(collector);
        request.setSlaViolated(false);
        request.setStatus(CollectionRequestStatus.ON_THE_WAY);

        when(collectionRequestRepository.findSlaViolatedTasks(any())).thenReturn(List.of(request));
        when(collectionRequestRepository.getReferenceById(201)).thenReturn(request);
        when(collectorRepository.getReferenceById(20)).thenReturn(collector);
        when(collectionRequestRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(collectionTrackingRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(collectorRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.checkSlaViolations();

        assertThat(request.getSlaViolated()).isTrue();
        assertThat(collector.getViolationCount()).isEqualTo(3);
        assertThat(collector.getStatus()).isEqualTo(CollectorStatus.SUSPEND);
    }
}
