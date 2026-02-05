package com.team2.Crowdsourced_Waste_Collection_Recycling_System.service.impl;

import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.CollectionRequest;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.Enterprise;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.WasteReport;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.enums.CollectionRequestStatus;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.enums.WasteReportStatus;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.collector.CollectionRequestRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.citizen.WasteReportRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.enterprise.EnterpriseRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.service.EnterpriseRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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

        // Check if CollectionRequest already exists for this report
        if (collectionRequestRepository.existsByReport_Id(wasteReport.getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Waste Report này đã có Collection Request");
        }

        // Generate unique request code
        String requestCode = generateRequestCode();

        // Create CollectionRequest
        CollectionRequest collectionRequest = new CollectionRequest();
        collectionRequest.setRequestCode(requestCode);
        collectionRequest.setReport(wasteReport);
        collectionRequest.setEnterprise(enterprise);
        collectionRequest.setStatus(CollectionRequestStatus.PENDING);
        collectionRequest.setCreatedAt(LocalDateTime.now());
        collectionRequest.setUpdatedAt(LocalDateTime.now());

        CollectionRequest savedRequest = collectionRequestRepository.save(collectionRequest);

        // Update WasteReport status to ACCEPTED_ENTERPRISE
        wasteReport.setStatus(WasteReportStatus.ACCEPTED_ENTERPRISE);
        wasteReport.setUpdatedAt(LocalDateTime.now());
        wasteReportRepository.save(wasteReport);

        return savedRequest.getId();
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
}
