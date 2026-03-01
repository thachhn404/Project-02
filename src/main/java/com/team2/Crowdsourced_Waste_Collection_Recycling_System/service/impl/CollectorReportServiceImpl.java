package com.team2.Crowdsourced_Waste_Collection_Recycling_System.service.impl;

import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.request.CollectorReportItemRequest;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.request.CreateCollectorReportRequest;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response.CloudinaryResponse;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response.CollectorReportResponse;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.*;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.enums.CollectionRequestStatus;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.enums.CollectorReportStatus;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.enums.WasteUnit;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.exception.AppException;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.exception.ErrorCode;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.collector.CollectionRequestRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.collector.CollectorReportRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.collector.CollectorRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.waste.WasteCategoryRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.service.CloudinaryService;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.service.CollectorReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CollectorReportServiceImpl implements CollectorReportService {

    private final CollectorReportRepository collectorReportRepository;
    private final CollectionRequestRepository collectionRequestRepository;
    private final CollectorRepository collectorRepository;
    private final WasteCategoryRepository wasteCategoryRepository;
    private final CloudinaryService cloudinaryService;

    @Override
    @Transactional
    public CollectorReportResponse createCollectorReport(CreateCollectorReportRequest request, List<MultipartFile> images, Integer collectorId) {
        // 1. Validate Collection Request
        CollectionRequest collectionRequest = collectionRequestRepository.findById(request.getCollectionRequestId())
                .orElseThrow(() -> new AppException(ErrorCode.COLLECTION_REQUEST_NOT_FOUND));

        // 2. Validate Collector
        Collector collector = collectorRepository.findById(collectorId)
                .orElseThrow(() -> new AppException(ErrorCode.COLLECTOR_NOT_FOUND));

        if (!collectionRequest.getCollector().getId().equals(collectorId)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        // 3. Create Report
        CollectorReport report = new CollectorReport();
        report.setCollectionRequest(collectionRequest);
        report.setCollector(collector);
        report.setReportCode("CR-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        report.setStatus(CollectorReportStatus.COMPLETED); // Or whatever initial status
        report.setCollectorNote(request.getCollectorNote());
        report.setCollectedAt(LocalDateTime.now());
        if(request.getLatitude() != null) report.setLatitude(BigDecimal.valueOf(request.getLatitude()));
        if(request.getLongitude() != null) report.setLongitude(BigDecimal.valueOf(request.getLongitude()));
        report.setCreatedAt(LocalDateTime.now());

        List<String> uploadedImageUrls = new ArrayList<>();
        if (images != null && !images.isEmpty()) {
            for (MultipartFile image : images) {
                CloudinaryResponse uploadResult = cloudinaryService.uploadImage(image, "collectorReport");
                if (uploadResult != null && uploadResult.getUrl() != null) {
                    uploadedImageUrls.add(uploadResult.getUrl());
                }
            }
        }

        // 4. Process Items (compute points/weight, do not persist item rows)
        int totalReportPoint = 0;
        BigDecimal computedWeightKg = BigDecimal.ZERO;
        boolean hasKg = false;

        if (request.getItems() != null) {
            for (CollectorReportItemRequest itemRequest : request.getItems()) {
                if (itemRequest == null || itemRequest.getCategoryId() == null || itemRequest.getQuantity() == null) {
                    continue;
                }

                WasteCategory category = wasteCategoryRepository.findById(itemRequest.getCategoryId())
                        .orElseThrow(() -> new AppException(ErrorCode.INVALID_REQUEST));
                BigDecimal pointPerUnit = category.getPointPerUnit() != null ? category.getPointPerUnit() : BigDecimal.ZERO;

                BigDecimal points = itemRequest.getQuantity().multiply(pointPerUnit);
                totalReportPoint += points.intValue();

                if (category.getUnit() == WasteUnit.KG) {
                    computedWeightKg = computedWeightKg.add(itemRequest.getQuantity());
                    hasKg = true;
                }
            }
        }

        report.setTotalPoint(totalReportPoint);

        // 5. Save Report first to get ID
        CollectorReport savedReport = collectorReportRepository.save(report);

        // 6. Update Collection Request Status
        collectionRequest.setStatus(CollectionRequestStatus.COMPLETED);
        collectionRequest.setCompletedAt(LocalDateTime.now());
        BigDecimal actualWeightKg = request.getActualWeightKg();
        if (actualWeightKg == null && hasKg && computedWeightKg.compareTo(BigDecimal.ZERO) > 0) {
            actualWeightKg = computedWeightKg;
        }
        if (actualWeightKg != null) {
            collectionRequest.setActualWeightKg(actualWeightKg.setScale(2, RoundingMode.HALF_UP));
        }
        collectionRequestRepository.save(collectionRequest);

        CollectorReportResponse response = mapToResponse(savedReport);
        response.setImageUrls(uploadedImageUrls);
        return response;
    }

    @Override
    public CollectorReportResponse getReportByCollectionRequest(Integer requestId, Integer collectorId) {
        CollectorReport report = collectorReportRepository.findByCollectionRequest_Id(requestId)
                .orElseThrow(() -> new RuntimeException("Report not found for request ID: " + requestId));
        
        if (!report.getCollector().getId().equals(collectorId)) {
             throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        
        return mapToResponse(report);
    }

    @Override
    public List<CollectorReportResponse> getReportsByCollector(Integer collectorId) {
        List<CollectorReport> reports = collectorReportRepository.findByCollector_Id(collectorId);
        return reports.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    public CollectorReportResponse getReportById(Integer reportId, Integer collectorId) {
        CollectorReport report = collectorReportRepository.findById(reportId)
                .orElseThrow(() -> new RuntimeException("Report not found with ID: " + reportId));

        if (!report.getCollector().getId().equals(collectorId)) {
             throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        return mapToResponse(report);
    }

    private CollectorReportResponse mapToResponse(CollectorReport report) {
        return CollectorReportResponse.builder()
                .id(report.getId())
                .reportCode(report.getReportCode())
                .collectionRequestId(report.getCollectionRequest().getId())
                .collectorId(report.getCollector().getId())
                .status(report.getStatus())
                .collectorNote(report.getCollectorNote())
                .totalPoint(report.getTotalPoint())
                .collectedAt(report.getCollectedAt())
                .latitude(report.getLatitude())
                .longitude(report.getLongitude())
                .createdAt(report.getCreatedAt())
                .imageUrls(new ArrayList<>())
                .build();
    }
}
