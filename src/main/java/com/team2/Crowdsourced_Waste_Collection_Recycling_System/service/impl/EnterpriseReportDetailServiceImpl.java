package com.team2.Crowdsourced_Waste_Collection_Recycling_System.service.impl;

import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response.CollectorReportResponse;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response.EnterpriseRequestReportDetailResponse;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response.EnterpriseWasteReportResponse;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.CollectionRequest;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.CollectorReport;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.WasteReport;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.collector.CollectionRequestRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.collector.CollectorReportImageRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.collector.CollectorReportRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.service.EnterpriseReportDetailService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class EnterpriseReportDetailServiceImpl implements EnterpriseReportDetailService {
    private final CollectionRequestRepository collectionRequestRepository;
    private final CollectorReportRepository collectorReportRepository;
    private final CollectorReportImageRepository collectorReportImageRepository;

    @Override
    public EnterpriseRequestReportDetailResponse getRequestReportDetail(Integer enterpriseId, Integer requestId) {
        if (enterpriseId == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User hiện tại không phải Enterprise");
        }
        CollectionRequest request = collectionRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Yêu cầu không tồn tại"));
        if (request.getEnterprise() == null || request.getEnterprise().getId() == null
                || !request.getEnterprise().getId().equals(enterpriseId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Yêu cầu không tồn tại");
        }

        WasteReport report = request.getReport();
        EnterpriseWasteReportResponse wasteReport = report == null ? null : EnterpriseWasteReportResponse.builder()
                .id(report.getId())
                .reportCode(report.getReportCode())
                .status(report.getStatus() != null ? report.getStatus().name() : null)
                .wasteType(report.getWasteType())
                .description(report.getDescription())
                .address(report.getAddress())
                .latitude(report.getLatitude())
                .longitude(report.getLongitude())
                .images(report.getImages())
                .createdAt(report.getCreatedAt())
                .build();

        CollectorReportResponse collectorReport = collectorReportRepository.findByCollectionRequest_Id(requestId)
                .map(r -> toCollectorReportResponse(r))
                .orElse(null);

        return EnterpriseRequestReportDetailResponse.builder()
                .requestId(request.getId())
                .requestCode(request.getRequestCode())
                .requestStatus(request.getStatus() != null ? request.getStatus().name() : null)
                .assignedAt(request.getAssignedAt())
                .acceptedAt(request.getAcceptedAt())
                .startedAt(request.getStartedAt())
                .collectedAt(request.getCollectedAt())
                .completedAt(request.getCompletedAt())
                .actualWeightKg(request.getActualWeightKg())
                .wasteReport(wasteReport)
                .collectorReport(collectorReport)
                .build();
    }

    private CollectorReportResponse toCollectorReportResponse(CollectorReport report) {
        return CollectorReportResponse.builder()
                .id(report.getId())
                .reportCode(report.getReportCode())
                .collectionRequestId(report.getCollectionRequest() != null ? report.getCollectionRequest().getId() : null)
                .collectorId(report.getCollector() != null ? report.getCollector().getId() : null)
                .status(report.getStatus())
                .collectorNote(report.getCollectorNote())
                .totalPoint(report.getTotalPoint())
                .collectedAt(report.getCollectedAt())
                .latitude(report.getLatitude())
                .longitude(report.getLongitude())
                .createdAt(report.getCreatedAt())
                .imageUrls(collectorReportImageRepository.findByCollectorReport_Id(report.getId()).stream()
                        .map(i -> i.getImageUrl())
                        .toList())
                .build();
    }
}
