package com.team2.Crowdsourced_Waste_Collection_Recycling_System.service;

import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.CollectionRequest;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.CollectionTracking;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.Collector;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.Enterprise;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.CollectionRequestRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.CollectionTrackingRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.CollectorRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.service.impl.EnterpriseAssignmentServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EnterpriseAssignmentServiceImplTest {
    @Mock
    CollectionRequestRepository collectionRequestRepository;
    @Mock
    CollectorRepository collectorRepository;
    @Mock
    CollectionTrackingRepository collectionTrackingRepository;

    @InjectMocks
    EnterpriseAssignmentServiceImpl service;

    @Captor
    ArgumentCaptor<CollectionTracking> trackingCaptor;

    @Test
    void assignCollector_success_updatesRequest_andCreatesTracking() {
        Enterprise enterprise = new Enterprise();
        enterprise.setId(10);

        CollectionRequest request = new CollectionRequest();
        request.setId(100);
        request.setEnterprise(enterprise);
        request.setStatus("pending");

        Collector collector = new Collector();
        collector.setId(200);
        collector.setEnterprise(enterprise);
        collector.setStatus("active");

        when(collectionRequestRepository.findById(100)).thenReturn(Optional.of(request));
        when(collectorRepository.findById(200)).thenReturn(Optional.of(collector));
        when(collectionRequestRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(collectionTrackingRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.assignCollector(10, 100, 200);

        assertEquals(100, response.getCollectionRequestId());
        assertEquals(200, response.getCollectorId());
        assertEquals("assigned", response.getStatus());
        assertNotNull(response.getAssignedAt());

        verify(collectionRequestRepository).save(request);
        verify(collectionTrackingRepository).save(trackingCaptor.capture());

        CollectionTracking tracking = trackingCaptor.getValue();
        assertEquals("assigned", tracking.getAction());
        assertEquals("Enterprise assigned collector", tracking.getNote());
        assertNotNull(tracking.getCreatedAt());
        assertSame(request, tracking.getCollectionRequest());
        assertSame(collector, tracking.getCollector());
    }

    @Test
    void assignCollector_rejects_whenStatusNotPending() {
        Enterprise enterprise = new Enterprise();
        enterprise.setId(10);

        CollectionRequest request = new CollectionRequest();
        request.setId(100);
        request.setEnterprise(enterprise);
        request.setStatus("accepted");

        when(collectionRequestRepository.findById(100)).thenReturn(Optional.of(request));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.assignCollector(10, 100, 200));

        assertEquals(400, ex.getStatusCode().value());
        verifyNoInteractions(collectorRepository);
        verify(collectionTrackingRepository, never()).save(any());
    }
}
