package com.team2.Crowdsourced_Waste_Collection_Recycling_System.service;

import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.request.CreateCollectorReportRequest;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response.CollectorReportResponse;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.*;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.enums.CollectionRequestStatus;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.citizen.WasteReportRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.collector.*;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.service.impl.CollectorReportServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response.CloudinaryResponse;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.service.CloudinaryService;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CollectorReportServiceImplTest {

    @Mock
    private CollectorReportRepository collectorReportRepository;
    @Mock
    private CollectorReportImageRepository collectorReportImageRepository;
    @Mock
    private CollectionRequestRepository collectionRequestRepository;
    @Mock
    private CollectionTrackingRepository collectionTrackingRepository;
    @Mock
    private CollectorRepository collectorRepository;
    @Mock
    private WasteReportRepository wasteReportRepository;
    @Mock
    private CloudinaryService cloudinaryService;

    @InjectMocks
    private CollectorReportServiceImpl collectorReportService;

    @Captor
    private ArgumentCaptor<CollectorReport> reportCaptor;

    @Captor
    private ArgumentCaptor<CollectionRequest> requestCaptor;

    @Test
    void createCollectorReport_success() {
        // Arrange
        Integer collectorId = 1;
        Integer requestId = 100;
        BigDecimal actualWeight = new BigDecimal("5.5");

        CreateCollectorReportRequest requestDto = new CreateCollectorReportRequest();
        requestDto.setCollectionRequestId(requestId);
        requestDto.setCollectorNote("Done");
        requestDto.setActualWeight(actualWeight);
        requestDto.setAddress("123 Test Street");
        
        MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", "test image content".getBytes());
        requestDto.setImages(Collections.singletonList(file));

        Collector collector = new Collector();
        collector.setId(collectorId);
        User user = new User();
        user.setFullName("Collector Name");
        collector.setUser(user);

        CollectionRequest collectionRequest = new CollectionRequest();
        collectionRequest.setId(requestId);
        collectionRequest.setCollector(collector);
        collectionRequest.setStatus(CollectionRequestStatus.ON_THE_WAY);
        WasteReport wasteReport = new WasteReport();
        collectionRequest.setReport(wasteReport);

        when(collectionRequestRepository.findById(requestId)).thenReturn(Optional.of(collectionRequest));
        when(collectorReportRepository.findByCollectionRequestId(requestId)).thenReturn(Optional.empty());
        when(collectorRepository.getReferenceById(collectorId)).thenReturn(collector);
        
        CollectorReport savedReport = new CollectorReport();
        savedReport.setId(1);
        savedReport.setCollector(collector);
        savedReport.setCollectionRequest(collectionRequest);
        savedReport.setActualWeight(actualWeight);
        
        when(collectorReportRepository.save(any(CollectorReport.class))).thenReturn(savedReport);
        when(cloudinaryService.uploadImage(any(), any())).thenReturn(CloudinaryResponse.builder().url("http://img.com/1.jpg").publicId("pid1").build());

        // Act
        CollectorReportResponse response = collectorReportService.createCollectorReport(requestDto, collectorId);

        // Assert
        verify(collectorReportRepository).save(reportCaptor.capture());
        CollectorReport capturedReport = reportCaptor.getValue();
        assertEquals(actualWeight, capturedReport.getActualWeight());
        assertEquals("Done", capturedReport.getCollectorNote());

        verify(collectionRequestRepository).save(requestCaptor.capture());
        CollectionRequest capturedRequest = requestCaptor.getValue();
        assertEquals(CollectionRequestStatus.COLLECTED, capturedRequest.getStatus());
        assertEquals(actualWeight, capturedRequest.getActualWeightKg());
        
        assertNotNull(response);
        assertEquals(actualWeight, response.getActualWeight());
    }
}
