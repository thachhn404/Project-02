package com.team2.Crowdsourced_Waste_Collection_Recycling_System.service.impl;

import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response.EnterpriseWasteReportResponse;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.Enterprise;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.WasteReport;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.enums.WasteReportStatus;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.enterprise.EnterpriseRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.waste.WasteReportRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.service.EnterpriseWasteReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EnterpriseWasteReportServiceImpl implements EnterpriseWasteReportService {

    private final WasteReportRepository wasteReportRepository;
    private final EnterpriseRepository enterpriseRepository;

    @Override
    public List<EnterpriseWasteReportResponse> getPendingReports(Integer enterpriseId) {
        if (enterpriseId == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User hiện tại không phải Enterprise");
        }

        Enterprise enterprise = enterpriseRepository.findById(enterpriseId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Enterprise không tồn tại"));

        List<WasteReport> pendingReports = wasteReportRepository.findByStatus(WasteReportStatus.PENDING);

        return pendingReports.stream()
                .filter(report -> isInServiceArea(enterprise, report))
                .map(this::toResponse)
                .toList();
    }

    private EnterpriseWasteReportResponse toResponse(WasteReport report) {
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
                .createdAt(report.getCreatedAt())
                .build();
    }

    @Override
    public EnterpriseWasteReportResponse getReportById(Integer enterpriseId, Integer reportId) {
        Enterprise enterprise = validateEnterprise(enterpriseId);
        WasteReport report = validateReport(reportId);

        if (!isInServiceArea(enterprise, report)) {
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
