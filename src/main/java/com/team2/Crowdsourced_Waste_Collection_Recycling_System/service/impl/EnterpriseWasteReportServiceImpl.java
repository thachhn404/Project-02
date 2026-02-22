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
                .filter(report -> isSupportedWasteType(enterprise, report.getWasteType()))
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

    private boolean isSupportedWasteType(Enterprise enterprise, String wasteType) {
        if (wasteType == null || wasteType.isBlank()) {
            return false;
        }
        String codes = enterprise.getSupportedWasteTypeCodes();
        if (codes == null || codes.isBlank()) {
            return false;
        }
        String target = wasteType.trim().toUpperCase();
        return Arrays.stream(codes.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(String::toUpperCase)
                .anyMatch(code -> code.equals(target));
    }

    @Override
    @Transactional
    public void acceptReport(Integer enterpriseId, Integer reportId) {
        Enterprise enterprise = validateEnterprise(enterpriseId);
        WasteReport report = validateReport(reportId);

        validateProcessingEligibility(enterprise, report);

        report.setStatus(WasteReportStatus.ACCEPTED_ENTERPRISE);
        report.setEnterprise(enterprise);
        report.setAcceptedAt(LocalDateTime.now());
        
        wasteReportRepository.save(report);
    }

    @Override
    @Transactional
    public void rejectReport(Integer enterpriseId, Integer reportId, String reason) {
        Enterprise enterprise = validateEnterprise(enterpriseId);
        WasteReport report = validateReport(reportId);

        // Enterprise vẫn có thể reject nếu không đủ điều kiện xử lý, nhưng ở đây ta giả sử họ reject vì lý do khác
        // Tuy nhiên, nếu họ không support loại rác này, họ không nên thấy nó để reject.
        // Nhưng logic nghiệp vụ cho phép reject, nên ta cứ để.
        
        // Check nếu report đã bị ai đó xử lý
        if (report.getStatus() != WasteReportStatus.PENDING) {
             throw new ResponseStatusException(HttpStatus.CONFLICT, "Báo cáo này đã được xử lý hoặc không còn ở trạng thái chờ");
        }

        report.setStatus(WasteReportStatus.REJECTED);
        report.setEnterprise(enterprise);
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
