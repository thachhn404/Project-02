package com.team2.Crowdsourced_Waste_Collection_Recycling_System.service;

import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.request.CollectorReportItemRequest;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.request.CreateCollectorReportRequest;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response.CollectorReportResponse;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.*;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.enums.CollectionRequestStatus;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.enums.WasteUnit;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.collector.*;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.profile.CitizenRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.reward.PointTransactionRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.waste.WasteCategoryRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.waste.WasteReportRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.service.impl.CollectorReportServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response.CloudinaryResponse;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CollectorReportServiceImplTest {

    @Mock
    private CollectorReportRepository collectorReportRepository;
    @Mock
    private CollectorReportImageRepository collectorReportImageRepository;
    @Mock
    private CollectorReportItemRepository collectorReportItemRepository;
    @Mock
    private CollectionRequestRepository collectionRequestRepository;
    @Mock
    private CollectionTrackingRepository collectionTrackingRepository;
    @Mock
    private CollectorRepository collectorRepository;
    @Mock
    private WasteReportRepository wasteReportRepository;
    @Mock
    private WasteCategoryRepository wasteCategoryRepository;
    @Mock
    private CitizenRepository citizenRepository;
    @Mock
    private PointTransactionRepository pointTransactionRepository;
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
        Integer collectorId = 1;
        Integer requestId = 100;

        CreateCollectorReportRequest requestDto = new CreateCollectorReportRequest();
        requestDto.setCollectionRequestId(requestId);
        requestDto.setCollectorNote("Done");
        requestDto.setWasteType("RECYCLABLE");
        requestDto.setItems(List.of(
                CollectorReportItemRequest.builder().categoryId(1).quantity(new BigDecimal("2.0")).build(),
                CollectorReportItemRequest.builder().categoryId(9).quantity(new BigDecimal("0.5")).build()
        ));

        MockMultipartFile file = new MockMultipartFile("images", "test.jpg", "image/jpeg", "test image content".getBytes());
        List<MultipartFile> images = Collections.singletonList(file);

        Collector collector = new Collector();
        collector.setId(collectorId);
        User user = new User();
        user.setFullName("Collector Name");
        collector.setUser(user);

        CollectionRequest collectionRequest = new CollectionRequest();
        collectionRequest.setId(requestId);
        collectionRequest.setCollector(collector);
        collectionRequest.setStatus(CollectionRequestStatus.COLLECTED);

        Citizen citizen = new Citizen();
        citizen.setId(77);
        citizen.setTotalPoints(100);

        WasteReport wasteReport = new WasteReport();
        wasteReport.setCitizen(citizen);
        collectionRequest.setReport(wasteReport);

        WasteCategory cat1 = new WasteCategory();
        cat1.setId(1);
        cat1.setName("Giấy");
        cat1.setUnit(WasteUnit.KG);
        cat1.setPointPerUnit(new BigDecimal("2250"));

        WasteCategory cat9 = new WasteCategory();
        cat9.setId(9);
        cat9.setName("Đồng");
        cat9.setUnit(WasteUnit.KG);
        cat9.setPointPerUnit(new BigDecimal("67500"));

        when(collectionRequestRepository.findById(requestId)).thenReturn(Optional.of(collectionRequest));
        when(collectorReportRepository.findByCollectionRequestId(requestId)).thenReturn(Optional.empty());
        when(collectorRepository.getReferenceById(collectorId)).thenReturn(collector);
        when(pointTransactionRepository.findByCollectionRequestId(requestId)).thenReturn(List.of());
        when(wasteCategoryRepository.findAllById(any())).thenReturn(List.of(cat1, cat9));
        when(citizenRepository.findByIdForUpdate(77)).thenReturn(Optional.of(citizen));

        when(collectorReportRepository.save(any(CollectorReport.class))).thenAnswer(invocation -> {
            CollectorReport input = invocation.getArgument(0);
            CollectorReport out = new CollectorReport();
            out.setId(input.getId() != null ? input.getId() : 1);
            out.setReportCode(input.getReportCode());
            out.setCollector(input.getCollector());
            out.setCollectionRequest(input.getCollectionRequest());
            out.setStatus(input.getStatus());
            out.setCollectorNote(input.getCollectorNote());
            out.setActualWeightRecyclable(input.getActualWeightRecyclable());
            out.setCollectedAt(input.getCollectedAt());
            out.setCreatedAt(input.getCreatedAt());
            out.setLatitude(input.getLatitude());
            out.setLongitude(input.getLongitude());
            out.setTotalPoint(input.getTotalPoint());
            return out;
        });
        when(cloudinaryService.uploadImage(any(), any())).thenReturn(CloudinaryResponse.builder().url("http://img.com/1.jpg").publicId("pid1").build());

        CollectorReportResponse response = collectorReportService.createCollectorReport(requestDto, images, collectorId);

        verify(collectorReportRepository, times(2)).save(reportCaptor.capture());
        CollectorReport firstSave = reportCaptor.getAllValues().getFirst();
        CollectorReport secondSave = reportCaptor.getAllValues().getLast();

        assertNull(firstSave.getReportCode());
        assertEquals("CR-000001", secondSave.getReportCode());
        assertEquals("Done", secondSave.getCollectorNote());
        assertEquals(38250, secondSave.getTotalPoint());

        verify(collectionRequestRepository).save(requestCaptor.capture());
        CollectionRequest capturedRequest = requestCaptor.getValue();
        assertEquals(new BigDecimal("2.5"), capturedRequest.getActualWeightKg());
        assertEquals(CollectionRequestStatus.COMPLETED, capturedRequest.getStatus());
        assertNotNull(capturedRequest.getCompletedAt());

        verify(collectionRequestRepository, never()).completeTask(anyInt(), anyInt(), any(LocalDateTime.class));
        verify(collectorReportItemRepository).saveAll(anyList());

        assertNotNull(response);
        assertEquals("CR-000001", response.getReportCode());
        assertEquals(38250, response.getTotalPoint());
    }

    @Test
    void createCollectorReport_success_withCategoryName() {
        Integer collectorId = 1;
        Integer requestId = 100;

        CreateCollectorReportRequest requestDto = new CreateCollectorReportRequest();
        requestDto.setCollectionRequestId(requestId);
        requestDto.setCollectorNote("Done");
        requestDto.setWasteType("RECYCLABLE");
        requestDto.setItems(List.of(
                CollectorReportItemRequest.builder().categoryName("Giấy").quantity(new BigDecimal("2.0")).build(),
                CollectorReportItemRequest.builder().categoryName("Đồng").quantity(new BigDecimal("0.5")).build()
        ));

        MockMultipartFile file = new MockMultipartFile("images", "test.jpg", "image/jpeg", "test image content".getBytes());
        List<MultipartFile> images = Collections.singletonList(file);

        Collector collector = new Collector();
        collector.setId(collectorId);
        User user = new User();
        user.setFullName("Collector Name");
        collector.setUser(user);

        CollectionRequest collectionRequest = new CollectionRequest();
        collectionRequest.setId(requestId);
        collectionRequest.setCollector(collector);
        collectionRequest.setStatus(CollectionRequestStatus.COLLECTED);

        Citizen citizen = new Citizen();
        citizen.setId(77);
        citizen.setTotalPoints(100);

        WasteReport wasteReport = new WasteReport();
        wasteReport.setCitizen(citizen);
        collectionRequest.setReport(wasteReport);

        WasteCategory cat1 = new WasteCategory();
        cat1.setId(1);
        cat1.setName("Giấy");
        cat1.setUnit(WasteUnit.KG);
        cat1.setPointPerUnit(new BigDecimal("2250"));

        WasteCategory cat9 = new WasteCategory();
        cat9.setId(9);
        cat9.setName("Đồng");
        cat9.setUnit(WasteUnit.KG);
        cat9.setPointPerUnit(new BigDecimal("67500"));

        when(collectionRequestRepository.findById(requestId)).thenReturn(Optional.of(collectionRequest));
        when(collectorReportRepository.findByCollectionRequestId(requestId)).thenReturn(Optional.empty());
        when(collectorRepository.getReferenceById(collectorId)).thenReturn(collector);
        when(pointTransactionRepository.findByCollectionRequestId(requestId)).thenReturn(List.of());
        when(wasteCategoryRepository.findByNameIgnoreCase("Giấy")).thenReturn(Optional.of(cat1));
        when(wasteCategoryRepository.findByNameIgnoreCase("Đồng")).thenReturn(Optional.of(cat9));
        when(wasteCategoryRepository.findAllById(any())).thenReturn(List.of(cat1, cat9));
        when(citizenRepository.findByIdForUpdate(77)).thenReturn(Optional.of(citizen));

        when(collectorReportRepository.save(any(CollectorReport.class))).thenAnswer(invocation -> {
            CollectorReport input = invocation.getArgument(0);
            CollectorReport out = new CollectorReport();
            out.setId(input.getId() != null ? input.getId() : 1);
            out.setReportCode(input.getReportCode());
            out.setCollector(input.getCollector());
            out.setCollectionRequest(input.getCollectionRequest());
            out.setStatus(input.getStatus());
            out.setCollectorNote(input.getCollectorNote());
            out.setActualWeightRecyclable(input.getActualWeightRecyclable());
            out.setCollectedAt(input.getCollectedAt());
            out.setCreatedAt(input.getCreatedAt());
            out.setLatitude(input.getLatitude());
            out.setLongitude(input.getLongitude());
            out.setTotalPoint(input.getTotalPoint());
            return out;
        });
        when(cloudinaryService.uploadImage(any(), any())).thenReturn(CloudinaryResponse.builder().url("http://img.com/1.jpg").publicId("pid1").build());

        CollectorReportResponse response = collectorReportService.createCollectorReport(requestDto, images, collectorId);

        assertNotNull(response);
        assertEquals("CR-000001", response.getReportCode());
        assertEquals(38250, response.getTotalPoint());

        verify(wasteCategoryRepository).findByNameIgnoreCase("Giấy");
        verify(wasteCategoryRepository).findByNameIgnoreCase("Đồng");
    }

    @Test
    void createCollectorReport_rejectsNonIntegerQuantityForCan() {
        Integer collectorId = 1;
        Integer requestId = 100;

        CreateCollectorReportRequest requestDto = new CreateCollectorReportRequest();
        requestDto.setCollectionRequestId(requestId);
        requestDto.setCollectorNote("Done");
        requestDto.setWasteType("RECYCLABLE");
        requestDto.setItems(List.of(
                CollectorReportItemRequest.builder().categoryId(5).quantity(new BigDecimal("1.5")).build()
        ));

        MockMultipartFile file = new MockMultipartFile("images", "test.jpg", "image/jpeg", "test image content".getBytes());
        List<MultipartFile> images = Collections.singletonList(file);

        Collector collector = new Collector();
        collector.setId(collectorId);
        User user = new User();
        user.setFullName("Collector Name");
        collector.setUser(user);

        CollectionRequest collectionRequest = new CollectionRequest();
        collectionRequest.setId(requestId);
        collectionRequest.setCollector(collector);
        collectionRequest.setStatus(CollectionRequestStatus.COLLECTED);
        collectionRequest.setReport(new WasteReport());

        WasteCategory cat5 = new WasteCategory();
        cat5.setId(5);
        cat5.setName("Lon bia");
        cat5.setUnit(WasteUnit.CAN);
        cat5.setPointPerUnit(new BigDecimal("180"));

        when(collectionRequestRepository.findById(requestId)).thenReturn(Optional.of(collectionRequest));
        when(collectorReportRepository.findByCollectionRequestId(requestId)).thenReturn(Optional.empty());
        when(pointTransactionRepository.findByCollectionRequestId(requestId)).thenReturn(List.of());
        when(wasteCategoryRepository.findAllById(any())).thenReturn(List.of(cat5));

        assertThrows(org.springframework.web.server.ResponseStatusException.class,
                () -> collectorReportService.createCollectorReport(requestDto, images, collectorId));
        verify(collectorReportRepository, never()).save(any(CollectorReport.class));
        verify(collectorReportItemRepository, never()).saveAll(anyList());
    }

    @Test
    void createCollectorReport_weightCountsOnlyKgItems() {
        Integer collectorId = 1;
        Integer requestId = 100;

        CreateCollectorReportRequest requestDto = new CreateCollectorReportRequest();
        requestDto.setCollectionRequestId(requestId);
        requestDto.setCollectorNote("Done");
        requestDto.setWasteType("RECYCLABLE");
        requestDto.setItems(List.of(
                CollectorReportItemRequest.builder().categoryId(1).quantity(new BigDecimal("2.5")).build(),
                CollectorReportItemRequest.builder().categoryId(5).quantity(new BigDecimal("10")).build()
        ));

        MockMultipartFile file = new MockMultipartFile("images", "test.jpg", "image/jpeg", "test image content".getBytes());
        List<MultipartFile> images = Collections.singletonList(file);

        Collector collector = new Collector();
        collector.setId(collectorId);
        User user = new User();
        user.setFullName("Collector Name");
        collector.setUser(user);

        CollectionRequest collectionRequest = new CollectionRequest();
        collectionRequest.setId(requestId);
        collectionRequest.setCollector(collector);
        collectionRequest.setStatus(CollectionRequestStatus.COLLECTED);

        Citizen citizen = new Citizen();
        citizen.setId(77);
        citizen.setTotalPoints(0);
        WasteReport wasteReport = new WasteReport();
        wasteReport.setCitizen(citizen);
        collectionRequest.setReport(wasteReport);

        WasteCategory cat1 = new WasteCategory();
        cat1.setId(1);
        cat1.setName("Giấy");
        cat1.setUnit(WasteUnit.KG);
        cat1.setPointPerUnit(new BigDecimal("2250"));

        WasteCategory cat5 = new WasteCategory();
        cat5.setId(5);
        cat5.setName("Lon bia");
        cat5.setUnit(WasteUnit.CAN);
        cat5.setPointPerUnit(new BigDecimal("180"));

        when(collectionRequestRepository.findById(requestId)).thenReturn(Optional.of(collectionRequest));
        when(collectorReportRepository.findByCollectionRequestId(requestId)).thenReturn(Optional.empty());
        when(collectorRepository.getReferenceById(collectorId)).thenReturn(collector);
        when(pointTransactionRepository.findByCollectionRequestId(requestId)).thenReturn(List.of());
        when(wasteCategoryRepository.findAllById(any())).thenReturn(List.of(cat1, cat5));
        when(citizenRepository.findByIdForUpdate(77)).thenReturn(Optional.of(citizen));

        when(collectorReportRepository.save(any(CollectorReport.class))).thenAnswer(invocation -> {
            CollectorReport input = invocation.getArgument(0);
            CollectorReport out = new CollectorReport();
            out.setId(input.getId() != null ? input.getId() : 1);
            out.setReportCode(input.getReportCode());
            out.setCollector(input.getCollector());
            out.setCollectionRequest(input.getCollectionRequest());
            out.setStatus(input.getStatus());
            out.setCollectorNote(input.getCollectorNote());
            out.setActualWeightRecyclable(input.getActualWeightRecyclable());
            out.setCollectedAt(input.getCollectedAt());
            out.setCreatedAt(input.getCreatedAt());
            out.setLatitude(input.getLatitude());
            out.setLongitude(input.getLongitude());
            out.setTotalPoint(input.getTotalPoint());
            return out;
        });
        when(cloudinaryService.uploadImage(any(), any())).thenReturn(CloudinaryResponse.builder().url("http://img.com/1.jpg").publicId("pid1").build());

        collectorReportService.createCollectorReport(requestDto, images, collectorId);

        verify(collectionRequestRepository).save(requestCaptor.capture());
        CollectionRequest capturedRequest = requestCaptor.getValue();
        assertEquals(new BigDecimal("2.5"), capturedRequest.getActualWeightKg());
    }
}
