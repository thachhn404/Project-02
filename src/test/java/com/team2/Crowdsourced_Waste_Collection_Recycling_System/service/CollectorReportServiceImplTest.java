package com.team2.Crowdsourced_Waste_Collection_Recycling_System.service;

import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.request.CollectorReportItemRequest;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.request.CreateCollectorReportRequest;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response.CollectorReportResponse;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.CollectionRequest;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.Collector;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.WasteCategory;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.enums.WasteUnit;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.collector.CollectionRequestRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.collector.CollectorReportRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.collector.CollectorRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.waste.WasteCategoryRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.service.impl.CollectorReportServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CollectorReportServiceImplTest {

    @Mock
    private CollectorReportRepository collectorReportRepository;
    @Mock
    private CollectionRequestRepository collectionRequestRepository;
    @Mock
    private CollectorRepository collectorRepository;
    @Mock
    private WasteCategoryRepository wasteCategoryRepository;
    @Mock
    private CloudinaryService cloudinaryService;

    @InjectMocks
    private CollectorReportServiceImpl service;

    @Test
    void createCollectorReport_savesActualWeightKg_whenProvided() {
        Integer requestId = 10;
        Integer collectorId = 1;

        Collector collector = new Collector();
        collector.setId(collectorId);

        CollectionRequest collectionRequest = new CollectionRequest();
        collectionRequest.setId(requestId);
        collectionRequest.setCollector(collector);

        WasteCategory kgCategory = new WasteCategory();
        kgCategory.setId(100);
        kgCategory.setUnit(WasteUnit.KG);
        kgCategory.setPointPerUnit(BigDecimal.ONE);

        when(collectionRequestRepository.findById(requestId)).thenReturn(Optional.of(collectionRequest));
        when(collectorRepository.findById(collectorId)).thenReturn(Optional.of(collector));
        when(wasteCategoryRepository.findById(100)).thenReturn(Optional.of(kgCategory));
        when(collectorReportRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        CreateCollectorReportRequest request = CreateCollectorReportRequest.builder()
                .collectionRequestId(requestId)
                .actualWeightKg(new BigDecimal("8.50"))
                .items(List.of(CollectorReportItemRequest.builder()
                        .categoryId(100)
                        .quantity(new BigDecimal("2"))
                        .build()))
                .build();

        CollectorReportResponse response = service.createCollectorReport(request, Collections.emptyList(), collectorId);

        ArgumentCaptor<CollectionRequest> captor = ArgumentCaptor.forClass(CollectionRequest.class);
        verify(collectionRequestRepository).save(captor.capture());
        assertThat(captor.getValue().getActualWeightKg()).isEqualByComparingTo("8.50");
        assertThat(captor.getValue().getCompletedAt()).isNotNull();
        assertThat(response).isNotNull();
    }

    @Test
    void createCollectorReport_computesActualWeightKg_fromKgItems_whenMissing() {
        Integer requestId = 11;
        Integer collectorId = 2;

        Collector collector = new Collector();
        collector.setId(collectorId);

        CollectionRequest collectionRequest = new CollectionRequest();
        collectionRequest.setId(requestId);
        collectionRequest.setCollector(collector);

        WasteCategory kgCategory = new WasteCategory();
        kgCategory.setId(200);
        kgCategory.setUnit(WasteUnit.KG);
        kgCategory.setPointPerUnit(BigDecimal.ONE);

        WasteCategory canCategory = new WasteCategory();
        canCategory.setId(201);
        canCategory.setUnit(WasteUnit.CAN);
        canCategory.setPointPerUnit(BigDecimal.ONE);

        when(collectionRequestRepository.findById(requestId)).thenReturn(Optional.of(collectionRequest));
        when(collectorRepository.findById(collectorId)).thenReturn(Optional.of(collector));
        when(wasteCategoryRepository.findById(200)).thenReturn(Optional.of(kgCategory));
        when(wasteCategoryRepository.findById(201)).thenReturn(Optional.of(canCategory));
        when(collectorReportRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        CreateCollectorReportRequest request = CreateCollectorReportRequest.builder()
                .collectionRequestId(requestId)
                .items(List.of(
                        CollectorReportItemRequest.builder().categoryId(200).quantity(new BigDecimal("1.235")).build(),
                        CollectorReportItemRequest.builder().categoryId(201).quantity(new BigDecimal("10")).build()
                ))
                .build();

        service.createCollectorReport(request, Collections.emptyList(), collectorId);

        ArgumentCaptor<CollectionRequest> captor = ArgumentCaptor.forClass(CollectionRequest.class);
        verify(collectionRequestRepository).save(captor.capture());
        assertThat(captor.getValue().getActualWeightKg()).isEqualByComparingTo("1.24");
        assertThat(captor.getValue().getCompletedAt()).isNotNull();
    }
}
