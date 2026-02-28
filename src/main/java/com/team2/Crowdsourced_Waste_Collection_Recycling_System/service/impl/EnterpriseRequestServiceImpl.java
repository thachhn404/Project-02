package com.team2.Crowdsourced_Waste_Collection_Recycling_System.service.impl;

import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.CollectionRequest;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.Enterprise;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.WasteReport;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.enums.CollectionRequestStatus;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.enums.WasteReportStatus;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.collector.CollectionRequestRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.enterprise.EnterpriseRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.waste.WasteReportRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.service.EnterpriseRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class EnterpriseRequestServiceImpl implements EnterpriseRequestService {
    private final CollectionRequestRepository collectionRequestRepository;
    private final WasteReportRepository wasteReportRepository;
    private final EnterpriseRepository enterpriseRepository;

    @Override
    @Transactional
    public Integer acceptWasteReport(Integer enterpriseId, String reportCode) {
        return acceptWasteReport(enterpriseId, reportCode, null);
    }

    @Override
    @Transactional
    public Integer acceptWasteReport(Integer enterpriseId, String reportCode, java.math.BigDecimal estimatedWeight) {
        // Validate input
        if (enterpriseId == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User hiện tại không phải Enterprise");
        }
        if (reportCode == null || reportCode.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Thiếu report_code");
        }

        // Validate enterprise exists
        Enterprise enterprise = enterpriseRepository.findById(enterpriseId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Enterprise không tồn tại"));

        // Find WasteReport
        WasteReport wasteReport = wasteReportRepository.findByReportCode(reportCode)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Waste Report không tồn tại"));

        // Validate WasteReport status is PENDING
        if (wasteReport.getStatus() != WasteReportStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Waste Report không ở trạng thái PENDING (hiện tại: " + wasteReport.getStatus() + ")");
        }

        if (!isSupportedWasteType(enterprise, wasteReport.getWasteType())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Loại rác không nằm trong năng lực xử lý của doanh nghiệp");
        }
        if (!isInServiceArea(enterprise, wasteReport)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Báo cáo không thuộc khu vực hoạt động của doanh nghiệp");
        }

        // Check if CollectionRequest already exists for this report
        if (collectionRequestRepository.existsByReport_Id(wasteReport.getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Waste Report này đã có Collection Request");
        }

        // Generate unique request code
        String requestCode = generateRequestCode();

        CollectionRequest collectionRequest = new CollectionRequest();
        collectionRequest.setRequestCode(requestCode);
        collectionRequest.setReport(wasteReport);
        collectionRequest.setEnterprise(enterprise);
        collectionRequest.setStatus(CollectionRequestStatus.PENDING);
        collectionRequest.setCreatedAt(LocalDateTime.now());
        collectionRequest.setUpdatedAt(LocalDateTime.now());

        CollectionRequest savedRequest = collectionRequestRepository.save(collectionRequest);

        wasteReport.setStatus(WasteReportStatus.ACCEPTED_ENTERPRISE);
        wasteReport.setAcceptedAt(LocalDateTime.now());
        wasteReport.setUpdatedAt(LocalDateTime.now());
        wasteReportRepository.save(wasteReport);

        return savedRequest.getId();
    }

    @Override
    @Transactional
    public void rejectWasteReport(Integer enterpriseId, String reportCode, String reason) {
        if (enterpriseId == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User hiện tại không phải Enterprise");
        }
        if (reportCode == null || reportCode.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Thiếu report_code");
        }

        Enterprise enterprise = enterpriseRepository.findById(enterpriseId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Enterprise không tồn tại"));

        WasteReport wasteReport = wasteReportRepository.findByReportCode(reportCode)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Waste Report không tồn tại"));

        if (wasteReport.getStatus() != WasteReportStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Báo cáo này đã được xử lý");
        }

        if (!isSupportedWasteType(enterprise, wasteReport.getWasteType()) || !isInServiceArea(enterprise, wasteReport)) {
            // vẫn cho phép reject nhưng đã lọc từ trước nên hiếm khi xảy ra
        }

        wasteReport.setStatus(WasteReportStatus.REJECTED);
        wasteReport.setRejectionReason(reason);
        wasteReport.setUpdatedAt(LocalDateTime.now());
        wasteReportRepository.save(wasteReport);
    }

    /**
     * Generate unique request code with format: CR-YYYYMMDD-XXXXX
     */
    private String generateRequestCode() {
        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String randomStr = String.format("%05d", new Random().nextInt(100000));
        String requestCode = "CR-" + dateStr + "-" + randomStr;

        // Check if code already exists, regenerate if needed
        while (collectionRequestRepository.findByRequestCode(requestCode).isPresent()) {
            randomStr = String.format("%05d", new Random().nextInt(100000));
            requestCode = "CR-" + dateStr + "-" + randomStr;
        }

        return requestCode;
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
