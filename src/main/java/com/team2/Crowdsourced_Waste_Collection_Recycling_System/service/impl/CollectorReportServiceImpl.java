package com.team2.Crowdsourced_Waste_Collection_Recycling_System.service.impl;

import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response.CollectorReportResponse;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response.ReportCollectorItemResponse;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response.ReportCollectorResponse;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.*;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.exception.AppException;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.exception.ErrorCode;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.collector.CollectorReportRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.collector.CollectionRequestRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.waste.ReportImageRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.waste.WasteReportItemRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.service.CollectorReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CollectorReportServiceImpl implements CollectorReportService {

    private final CollectorReportRepository collectorReportRepository;
    private final CollectionRequestRepository collectionRequestRepository;
    private final WasteReportItemRepository wasteReportItemRepository;
    private final ReportImageRepository reportImageRepository;

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

    @Override
    public ReportCollectorResponse getCreateReport(Integer requestId, Integer collectorId) {
        CollectionRequest request = collectionRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Collection Request không tồn tại"));

        if (request.getCollector() == null || request.getCollector().getId() == null
                || !request.getCollector().getId().equals(collectorId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Collection Request không thuộc về bạn");
        }

        WasteReport report = request.getReport();
        if (report == null || report.getId() == null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Collection Request không có Waste Report hợp lệ");
        }

        List<WasteReportItem> items = wasteReportItemRepository.findWithCategoryByReportId(report.getId());
        List<ReportCollectorItemResponse> itemResponses = items.stream()
                .map(i -> ReportCollectorItemResponse.builder()
                        .categoryId(i.getWasteCategory().getId())
                        .categoryName(i.getWasteCategory().getName())
                        .wasteUnit(i.getUnitSnapshot())
                        .suggestedQuantity(i.getQuantity())
                        .build())
                .toList();

        List<String> imageUrls = reportImageRepository.findByReport_Id(report.getId()).stream()
                .map(ReportImage::getImageUrl)
                .toList();

        return ReportCollectorResponse.builder()
                .collectionRequestId(request.getId())
                .wasteCollectionRequestId(report.getId())
                .wasteReportCode(report.getReportCode())
                .wasteType(report.getWasteType())
                .address(report.getAddress())
                .latitude(report.getLatitude())
                .longitude(report.getLongitude())
                .items(itemResponses)
                .imageUrls(imageUrls)
                .build();
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
