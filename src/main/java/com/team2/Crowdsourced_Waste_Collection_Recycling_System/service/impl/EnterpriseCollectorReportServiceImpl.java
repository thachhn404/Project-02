package com.team2.Crowdsourced_Waste_Collection_Recycling_System.service.impl;

import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response.CollectorReportResponse;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.CollectorReport;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.CollectorReportImage;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.Enterprise;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.enums.CollectorReportStatus;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.collector.CollectorReportImageRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.collector.CollectorReportRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.enterprise.EnterpriseRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.service.EnterpriseCollectorReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EnterpriseCollectorReportServiceImpl implements EnterpriseCollectorReportService {

    private final CollectorReportRepository collectorReportRepository;
    private final CollectorReportImageRepository collectorReportImageRepository;
    private final EnterpriseRepository enterpriseRepository;

    @Override
    public List<CollectorReportResponse> getCollectorReports(Integer enterpriseId, String status) {
        validateEnterprise(enterpriseId);

        CollectorReportStatus statusFilter = null;
        if (status != null && !status.isBlank()) {
            try {
                statusFilter = CollectorReportStatus.valueOf(status.trim().toUpperCase());
            } catch (IllegalArgumentException ex) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "status không hợp lệ");
            }
        }

        List<CollectorReport> reports = collectorReportRepository
                .findByCollectionRequest_Enterprise_IdOrderByCreatedAtDesc(enterpriseId);

        if (statusFilter != null) {
            CollectorReportStatus finalStatusFilter = statusFilter;
            reports = reports.stream()
                    .filter(r -> r.getStatus() == finalStatusFilter)
                    .toList();
        }

        List<Integer> reportIds = reports.stream()
                .map(CollectorReport::getId)
                .toList();

        Map<Integer, List<String>> imageUrlsByReportId = reportIds.isEmpty()
                ? Collections.emptyMap()
                : collectorReportImageRepository.findByCollectorReport_IdIn(reportIds).stream()
                .collect(Collectors.groupingBy(
                        img -> img.getCollectorReport().getId(),
                        Collectors.mapping(CollectorReportImage::getImageUrl, Collectors.toList())
                ));

        return reports.stream()
                .map(r -> CollectorReportResponse.builder()
                        .id(r.getId())
                        .reportCode(r.getReportCode())
                        .collectionRequestId(r.getCollectionRequest().getId())
                        .collectorId(r.getCollector().getId())
                        .status(r.getStatus())
                        .collectorNote(r.getCollectorNote())
                        .totalPoint(r.getTotalPoint())
                        .collectedAt(r.getCollectedAt())
                        .latitude(r.getLatitude())
                        .longitude(r.getLongitude())
                        .createdAt(r.getCreatedAt())
                        .imageUrls(imageUrlsByReportId.getOrDefault(r.getId(), List.of()))
                        .build())
                .toList();
    }

    private Enterprise validateEnterprise(Integer enterpriseId) {
        if (enterpriseId == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User hiện tại không phải Enterprise");
        }
        return enterpriseRepository.findById(enterpriseId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Enterprise không tồn tại"));
    }
}
