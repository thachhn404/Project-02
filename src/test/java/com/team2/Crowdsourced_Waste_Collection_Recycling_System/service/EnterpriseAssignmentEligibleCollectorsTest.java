package com.team2.Crowdsourced_Waste_Collection_Recycling_System.service;

import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response.EligibleCollectorResponse;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.CollectionRequest;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.Collector;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.Enterprise;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.WasteReport;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.enums.CollectionRequestStatus;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.enums.CollectorStatus;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.collector.CollectionRequestRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.collector.CollectorRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.service.impl.EnterpriseAssignmentServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EnterpriseAssignmentEligibleCollectorsTest {
    @Mock
    CollectionRequestRepository collectionRequestRepository;
    @Mock
    CollectorRepository collectorRepository;

    @InjectMocks
    EnterpriseAssignmentServiceImpl service;

    @Test
    void filtersCollectors_byRadius_andOnline_andStatus() {
        Enterprise ent = new Enterprise();
        ent.setId(1);

        WasteReport report = new WasteReport();
        report.setLatitude(BigDecimal.valueOf(10.0));
        report.setLongitude(BigDecimal.valueOf(106.0));

        CollectionRequest req = new CollectionRequest();
        req.setId(111);
        req.setEnterprise(ent);
        req.setStatus(CollectionRequestStatus.ACCEPTED_ENTERPRISE);
        req.setReport(report);

        when(collectionRequestRepository.findById(111)).thenReturn(Optional.of(req));

        Collector nearOnline = new Collector();
        nearOnline.setId(1);
        nearOnline.setEnterprise(ent);
        nearOnline.setStatus(CollectorStatus.ACTIVE);
        nearOnline.setFullName("Near Online");
        nearOnline.setCurrentLatitude(BigDecimal.valueOf(10.01));
        nearOnline.setCurrentLongitude(BigDecimal.valueOf(106.01));
        nearOnline.setLastLocationUpdate(LocalDateTime.now());

        Collector far = new Collector();
        far.setId(2);
        far.setEnterprise(ent);
        far.setStatus(CollectorStatus.ACTIVE);
        far.setFullName("Far");
        far.setCurrentLatitude(BigDecimal.valueOf(11.5));
        far.setCurrentLongitude(BigDecimal.valueOf(108.0));
        far.setLastLocationUpdate(LocalDateTime.now());

        Collector suspended = new Collector();
        suspended.setId(3);
        suspended.setEnterprise(ent);
        suspended.setStatus(CollectorStatus.SUSPEND);
        suspended.setFullName("Suspended");
        suspended.setCurrentLatitude(BigDecimal.valueOf(10.02));
        suspended.setCurrentLongitude(BigDecimal.valueOf(106.02));
        suspended.setLastLocationUpdate(LocalDateTime.now());

        Collector offline = new Collector();
        offline.setId(4);
        offline.setEnterprise(ent);
        offline.setStatus(CollectorStatus.ACTIVE);
        offline.setFullName("Offline");
        offline.setCurrentLatitude(BigDecimal.valueOf(10.02));
        offline.setCurrentLongitude(BigDecimal.valueOf(106.02));
        offline.setLastLocationUpdate(LocalDateTime.now().minusHours(2));

        when(collectorRepository.findByEnterprise_IdOrderByCreatedAtDesc(1))
                .thenReturn(List.of(nearOnline, far, suspended, offline));

        when(collectionRequestRepository.countByCollector_IdAndStatus(anyInt(), any()))
                .thenReturn(0L);

        List<EligibleCollectorResponse> out = service.findEligibleCollectors(1, 111, 10.0);
        assertEquals(2, out.size());
        assertEquals("Near Online", out.get(0).getFullName());
        assertTrue(out.get(0).getOnline());
        assertFalse(out.get(1).getOnline());
    }

    @Test
    void validateStatus_mustBeAcceptedEnterprise() {
        Enterprise ent = new Enterprise();
        ent.setId(1);
        CollectionRequest req = new CollectionRequest();
        req.setId(222);
        req.setEnterprise(ent);
        req.setStatus(CollectionRequestStatus.PENDING);
        when(collectionRequestRepository.findById(222)).thenReturn(Optional.of(req));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.findEligibleCollectors(1, 222, 10.0));
        assertEquals(400, ex.getStatusCode().value());
    }
}
