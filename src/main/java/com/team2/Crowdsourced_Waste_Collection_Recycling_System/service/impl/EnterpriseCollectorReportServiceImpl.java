package com.team2.Crowdsourced_Waste_Collection_Recycling_System.service.impl;

import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response.CollectorReportResponse;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response.WasteCategoryResponse;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.CollectorReport;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.CollectorReportImage;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.CollectorReportItem;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.Enterprise;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.collector.CollectorReportImageRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.collector.CollectorReportItemRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.collector.CollectorReportRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.enterprise.EnterpriseRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.service.EnterpriseCollectorReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EnterpriseCollectorReportServiceImpl implements EnterpriseCollectorReportService {

    private final CollectorReportRepository collectorReportRepository;
    private final CollectorReportImageRepository collectorReportImageRepository;
    private final CollectorReportItemRepository collectorReportItemRepository;
    private final EnterpriseRepository enterpriseRepository;

    @Override
    public List<CollectorReportResponse> getCollectorReports(Integer enterpriseId) {
        validateEnterprise(enterpriseId);

        List<CollectorReport> reports = collectorReportRepository
                .findByCollectionRequest_Enterprise_IdOrderByCreatedAtDesc(enterpriseId);

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

        Map<Integer, List<WasteCategoryResponse>> categoriesByReportId = reportIds.isEmpty()
                ? Collections.emptyMap()
                : collectorReportItemRepository.findWithCategoryByCollectorReportIdIn(reportIds).stream()
                .collect(Collectors.groupingBy(
                        item -> item.getCollectorReport().getId(),
                        Collectors.collectingAndThen(Collectors.toList(), this::toWasteCategoryResponses)
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
                        .categories(categoriesByReportId.getOrDefault(r.getId(), List.of()))
                        .build())
                .toList();
    }

    private List<WasteCategoryResponse> toWasteCategoryResponses(List<CollectorReportItem> items) {
        Map<Integer, WasteCategoryResponse> byCategoryId = new LinkedHashMap<>();
        for (CollectorReportItem item : items) {
            if (item.getWasteCategory() == null || item.getWasteCategory().getId() == null) {
                continue;
            }
            Integer categoryId = item.getWasteCategory().getId();
            WasteCategoryResponse existing = byCategoryId.get(categoryId);
            if (existing == null) {
                byCategoryId.put(categoryId, WasteCategoryResponse.builder()
                        .id(categoryId)
                        .name(item.getWasteCategory().getName())
                        .unit(item.getUnitSnapshot() != null ? item.getUnitSnapshot().name() : null)
                        .pointPerUnit(item.getPointPerUnitSnapshot())
                        .quantity(item.getQuantity())
                        .build());
            } else {
                if (existing.getQuantity() == null) {
                    existing.setQuantity(item.getQuantity());
                } else if (item.getQuantity() != null) {
                    existing.setQuantity(existing.getQuantity().add(item.getQuantity()));
                }
            }
        }
        return List.copyOf(byCategoryId.values());
    }

    private Enterprise validateEnterprise(Integer enterpriseId) {
        if (enterpriseId == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User hiện tại không phải Enterprise");
        }
        return enterpriseRepository.findById(enterpriseId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Enterprise không tồn tại"));
    }
}
