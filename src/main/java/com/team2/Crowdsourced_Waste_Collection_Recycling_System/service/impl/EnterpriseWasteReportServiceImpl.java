package com.team2.Crowdsourced_Waste_Collection_Recycling_System.service.impl;

import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response.EnterpriseWasteReportResponse;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response.WasteCategoryResponse;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.Enterprise;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.ReportImage;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.WasteReport;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.WasteReportItem;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.enums.WasteReportStatus;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.enterprise.EnterpriseRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.waste.ReportImageRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.waste.WasteReportItemRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.waste.WasteReportRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.service.EnterpriseWasteReportService;
import org.springframework.data.domain.Sort;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EnterpriseWasteReportServiceImpl implements EnterpriseWasteReportService {

    private final WasteReportRepository wasteReportRepository;
    private final EnterpriseRepository enterpriseRepository;
    private final ReportImageRepository reportImageRepository;
    private final WasteReportItemRepository wasteReportItemRepository;

    @Override
    public List<EnterpriseWasteReportResponse> getReports(Integer enterpriseId, String status) {
        Enterprise enterprise = null;
        if (enterpriseId != null) {
            enterprise = validateEnterprise(enterpriseId);
        }

        WasteReportStatus statusFilter = null;
        if (status != null && !status.isBlank()) {
            try {
                statusFilter = WasteReportStatus.valueOf(status.trim().toUpperCase());
            } catch (IllegalArgumentException ex) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "status không hợp lệ");
            }
        }

        List<WasteReport> reports = wasteReportRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));

        WasteReportStatus finalStatusFilter = statusFilter;
        Enterprise finalEnterprise = enterprise;
        return reports.stream()
                .filter(report -> finalStatusFilter == null || report.getStatus() == finalStatusFilter)
                .filter(report -> finalEnterprise == null || isInServiceArea(finalEnterprise, report))
                .map(this::toResponse)
                .toList();
    }

    @Override
    public List<EnterpriseWasteReportResponse> getPendingReports(Integer enterpriseId) {
        Enterprise enterprise = null;
        if (enterpriseId != null) {
            enterprise = validateEnterprise(enterpriseId);
        }

        List<WasteReport> pendingReports = wasteReportRepository.findByStatus(WasteReportStatus.PENDING);

        Enterprise finalEnterprise = enterprise;
        return pendingReports.stream()
                .filter(report -> finalEnterprise == null || isInServiceArea(finalEnterprise, report))
                .map(this::toResponse)
                .toList();
    }

    private EnterpriseWasteReportResponse toResponse(WasteReport report) {
        List<String> imageUrls = reportImageRepository.findByReport_Id(report.getId()).stream()
                .map(ReportImage::getImageUrl)
                .toList();

        List<WasteCategoryResponse> categories = toWasteCategoryResponses(
                wasteReportItemRepository.findWithCategoryByReportId(report.getId())
        );

        return EnterpriseWasteReportResponse.builder()
                .id(report.getId())
                .reportCode(report.getReportCode())
                .status(report.getStatus() != null ? report.getStatus().name() : null)
                .wasteType(report.getWasteType())
                .description(report.getDescription())
                .address(report.getAddress())
                .latitude(report.getLatitude())
                .longitude(report.getLongitude())
                .images(report.getImages())
                .imageUrls(imageUrls)
                .categories(categories)
                .createdAt(report.getCreatedAt())
                .build();
    }

    private List<WasteCategoryResponse> toWasteCategoryResponses(List<WasteReportItem> items) {
        Map<Integer, WasteCategoryResponse> byCategoryId = new LinkedHashMap<>();
        for (WasteReportItem item : items) {
            if (item.getWasteCategory() == null || item.getWasteCategory().getId() == null) {
                continue;
            }
            Integer categoryId = item.getWasteCategory().getId();
            WasteCategoryResponse existing = byCategoryId.get(categoryId);
            if (existing == null) {
                byCategoryId.put(categoryId, WasteCategoryResponse.builder()
                        .id(categoryId)
                        .name(item.getWasteCategory().getName())
                        .unit(item.getUnitSnapshot() != null ? item.getUnitSnapshot().name()
                                : (item.getWasteCategory().getUnit() != null ? item.getWasteCategory().getUnit().name() : null))
                        .pointPerUnit(item.getWasteCategory().getPointPerUnit())
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

    @Override
    public EnterpriseWasteReportResponse getReportById(Integer enterpriseId, Integer reportId) {
        Enterprise enterprise = null;
        if (enterpriseId != null) {
            enterprise = validateEnterprise(enterpriseId);
        }
        WasteReport report = validateReport(reportId);

        if (enterprise != null && !isInServiceArea(enterprise, report)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Báo cáo không tồn tại");
        }

        return toResponse(report);
    }

    @Override
    @Transactional
    public void acceptReport(Integer enterpriseId, Integer reportId) {
        WasteReport report = validateReport(reportId);
        report.setStatus(WasteReportStatus.ACCEPTED_ENTERPRISE);
        report.setAcceptedAt(LocalDateTime.now());
        
        wasteReportRepository.save(report);
    }

    @Override
    @Transactional
    public void rejectReport(Integer enterpriseId, Integer reportId, String reason) {
        WasteReport report = validateReport(reportId);
        report.setStatus(WasteReportStatus.REJECTED);
        report.setRejectionReason(reason);
        
        wasteReportRepository.save(report);
    }

    private Enterprise validateEnterprise(Integer enterpriseId) {
        if (enterpriseId == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User hiện tại không phải Enterprise");
        }
        return enterpriseRepository.findById(enterpriseId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Enterprise không tồn tại"));
    }

    private WasteReport validateReport(Integer reportId) {
        return wasteReportRepository.findById(reportId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Báo cáo không tồn tại"));
    }

    private void validateProcessingEligibility(Enterprise enterprise, WasteReport report) {
        if (report.getStatus() != WasteReportStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Báo cáo này đã được xử lý hoặc không còn ở trạng thái chờ");
        }
        
        // Kiểm tra xem Enterprise có xử lý được loại rác và khu vực này không
        // Nếu không, họ không nên được phép accept (để tránh tranh giành report mà mình không làm được)
        if (!isSupportedWasteType(enterprise, report.getWasteType())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Enterprise không hỗ trợ loại rác này");
        }
        
        if (!isInServiceArea(enterprise, report)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Báo cáo nằm ngoài khu vực hoạt động của Enterprise");
        }
    }

    private boolean isSupportedWasteType(Enterprise enterprise, String wasteType) {
        // TODO: Implement check for supported waste types
        return true;
    }

    private boolean isInServiceArea(Enterprise enterprise, WasteReport report) {
        String address = report.getAddress();
        if (address == null || address.isBlank()) {
            return false;
        }

        String wardList = enterprise.getServiceWards();
        String cityList = enterprise.getServiceCities();
        String lowerAddress = address.toLowerCase();

        boolean wardOk = wardList == null || wardList.isBlank()
                || Arrays.stream(wardList.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(String::toLowerCase)
                .anyMatch(lowerAddress::contains);

        boolean cityOk = cityList == null || cityList.isBlank()
                || Arrays.stream(cityList.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(String::toLowerCase)
                .anyMatch(lowerAddress::contains);

        return wardOk && cityOk;
    }
}
