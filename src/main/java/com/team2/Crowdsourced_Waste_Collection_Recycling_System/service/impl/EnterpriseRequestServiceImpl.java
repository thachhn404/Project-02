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
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
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
        if (reportCode == null || reportCode.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Thiếu report_code");
        }
        WasteReport wasteReport = wasteReportRepository.findByReportCode(reportCode)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Waste Report không tồn tại"));
        Enterprise enterprise = requireEnterprise(enterpriseId);

        // Idempotent behavior:
        // - If PENDING: accept and create CollectionRequest
        // - If already ACCEPTED_ENTERPRISE: ensure a CollectionRequest exists (create if missing) and return its id
        // - Otherwise: reject
        LocalDateTime now = LocalDateTime.now();
        if (wasteReport.getStatus() == null || wasteReport.getStatus() == WasteReportStatus.PENDING) {
            if (!isInServiceArea(enterprise, wasteReport)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Báo cáo nằm ngoài khu vực phục vụ của doanh nghiệp");
            }
            if (collectionRequestRepository.existsByReport_Id(wasteReport.getId())) {
                return collectionRequestRepository.findByReport_Id(wasteReport.getId())
                        .map(CollectionRequest::getId)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT, "Trạng thái không nhất quán"));
            }
            wasteReport.setStatus(WasteReportStatus.ACCEPTED_ENTERPRISE);
            wasteReport.setAcceptedAt(now);
            wasteReport.setUpdatedAt(now);
            wasteReportRepository.save(wasteReport);

            CollectionRequest cr = new CollectionRequest();
            cr.setRequestCode(generateRequestCode());
            cr.setReport(wasteReport);
            cr.setEnterprise(enterprise);
            cr.setStatus(CollectionRequestStatus.ACCEPTED_ENTERPRISE);
            cr.setCreatedAt(now);
            cr.setUpdatedAt(now);
            cr.setSlaViolated(false);
            collectionRequestRepository.save(cr);
            cr.setRequestCode(String.format("CR%03d", cr.getId()));
            collectionRequestRepository.save(cr);
            return cr.getId();
        } else if (wasteReport.getStatus() == WasteReportStatus.ACCEPTED_ENTERPRISE) {
            return collectionRequestRepository.findByReport_Id(wasteReport.getId())
                    .map(existing -> {
                        if (existing.getRequestCode() == null || !existing.getRequestCode().matches("^CR\\d{3}$")) {
                            existing.setRequestCode(String.format("CR%03d", existing.getId()));
                            collectionRequestRepository.save(existing);
                        }
                        return existing.getId();
                    })
                    .orElseGet(() -> {
                        CollectionRequest cr = new CollectionRequest();
                        cr.setRequestCode(generateRequestCode());
                        cr.setReport(wasteReport);
                        cr.setEnterprise(enterprise);
                        cr.setStatus(CollectionRequestStatus.ACCEPTED_ENTERPRISE);
                        cr.setCreatedAt(now);
                        cr.setUpdatedAt(now);
                        cr.setSlaViolated(false);
                        collectionRequestRepository.save(cr);
                        cr.setRequestCode(String.format("CR%03d", cr.getId()));
                        collectionRequestRepository.save(cr);
                        return cr.getId();
                    });
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Waste Report không ở trạng thái hợp lệ để accept");
        }
    }

    @Override
    @Transactional
    public void rejectWasteReport(Integer enterpriseId, String reportCode, String reason) {
        if (reportCode == null || reportCode.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Thiếu report_code");
        }
        WasteReport wasteReport = wasteReportRepository.findByReportCode(reportCode)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Waste Report không tồn tại"));
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

    private Enterprise requireEnterprise(Integer enterpriseId) {
        if (enterpriseId == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User hiện tại không phải Enterprise");
        }
        return enterpriseRepository.findById(enterpriseId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Enterprise không tồn tại"));
    }
}
