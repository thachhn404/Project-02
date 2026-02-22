package com.team2.Crowdsourced_Waste_Collection_Recycling_System.service;

import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.CollectionRequest;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.enums.CollectionRequestStatus;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.CollectionTracking;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.Collector;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.collector.CollectionRequestRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.collector.CollectionTrackingRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.collector.CollectorRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.waste.WasteReportRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.service.impl.CollectorServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CollectorServiceImplTest {
    @Mock
    CollectionRequestRepository collectionRequestRepository;
    @Mock
    CollectionTrackingRepository collectionTrackingRepository;
    @Mock
    CollectorRepository collectorRepository;
    @Mock
    WasteReportRepository wasteReportRepository;

    @InjectMocks
    CollectorServiceImpl service;

    @Captor
    ArgumentCaptor<CollectionTracking> trackingCaptor;

    @Test
    void acceptTask_success_createsTracking() {
        Collector collector = new Collector();
        collector.setId(200);

        CollectionRequest request = new CollectionRequest();
        request.setId(100);

        when(collectionRequestRepository.acceptTask(eq(100), eq(200), any(LocalDateTime.class))).thenReturn(1);
        when(collectionRequestRepository.getReferenceById(100)).thenReturn(request);
        when(collectorRepository.getReferenceById(200)).thenReturn(collector);
        when(collectionTrackingRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        service.acceptTask(100, 200);

        verify(collectionTrackingRepository).save(trackingCaptor.capture());
        CollectionTracking tracking = trackingCaptor.getValue();
        assertEquals("accepted", tracking.getAction());
        assertEquals("Collector accepted task", tracking.getNote());
        assertSame(request, tracking.getCollectionRequest());
        assertSame(collector, tracking.getCollector());
        assertNotNull(tracking.getCreatedAt());
    }

    @Test
    void startTask_success_updatesStatus_andCreatesTracking() {
        Collector collector = new Collector();
        collector.setId(200);

        CollectionRequest request = new CollectionRequest();
        request.setId(100);

        when(collectionRequestRepository.updateStatusIfMatch(eq(100), eq(200), eq(CollectionRequestStatus.ACCEPTED_COLLECTOR), eq(CollectionRequestStatus.ON_THE_WAY), any(LocalDateTime.class)))
                .thenReturn(1);
        when(collectionRequestRepository.getReferenceById(100)).thenReturn(request);
        when(collectorRepository.getReferenceById(200)).thenReturn(collector);
        when(collectionTrackingRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        service.startTask(100, 200);

        verify(collectionRequestRepository).updateStatusIfMatch(eq(100), eq(200), eq(CollectionRequestStatus.ACCEPTED_COLLECTOR), eq(CollectionRequestStatus.ON_THE_WAY), any(LocalDateTime.class));
        verify(collectionTrackingRepository).save(trackingCaptor.capture());
        CollectionTracking tracking = trackingCaptor.getValue();
        assertEquals("started", tracking.getAction());
        assertEquals("Collector started moving", tracking.getNote());
    }

    @Test
    void rejectTask_missingReason_throws400() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.rejectTask(100, 200, "  "));
        assertEquals(400, ex.getStatusCode().value());
        verifyNoInteractions(collectionRequestRepository);
        verifyNoInteractions(collectionTrackingRepository);
    }

    @Test
    void rejectTask_success_updates_andCreatesTracking() {
        Collector collector = new Collector();
        collector.setId(200);

        CollectionRequest request = new CollectionRequest();
        request.setId(100);

        when(collectionRequestRepository.rejectTask(100, 200, "bận")).thenReturn(1);
        when(collectionRequestRepository.getReferenceById(100)).thenReturn(request);
        when(collectorRepository.getReferenceById(200)).thenReturn(collector);
        when(collectionTrackingRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        service.rejectTask(100, 200, "bận");

        verify(collectionRequestRepository).rejectTask(100, 200, "bận");
        verify(collectionTrackingRepository).save(trackingCaptor.capture());
        CollectionTracking tracking = trackingCaptor.getValue();
        assertEquals("rejected", tracking.getAction());
        assertTrue(tracking.getNote().contains("bận"));
    }

    @Test
    void completeTask_success_updates_andCreatesTracking() {
        Collector collector = new Collector();
        collector.setId(200);

        CollectionRequest request = new CollectionRequest();
        request.setId(100);

        when(collectionRequestRepository.completeTask(eq(100), eq(200), any(LocalDateTime.class))).thenReturn(1);
        when(collectionRequestRepository.getReferenceById(100)).thenReturn(request);
        when(collectorRepository.getReferenceById(200)).thenReturn(collector);
        when(collectionTrackingRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        service.completeTask(100, 200);

        verify(collectionRequestRepository).completeTask(eq(100), eq(200), any(LocalDateTime.class));
        verify(collectionTrackingRepository).save(trackingCaptor.capture());
        CollectionTracking tracking = trackingCaptor.getValue();
        assertEquals("collected", tracking.getAction());
        assertEquals("Collector completed task", tracking.getNote());
    }
}
