package com.team2.Crowdsourced_Waste_Collection_Recycling_System.service.impl;

import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response.AssignCollectorResponse;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response.EligibleCollectorResponse;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response.RequestPreviewResponse;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.CollectionRequest;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.WasteReport;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.enums.CollectionRequestStatus;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.CollectionTracking;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.enums.CollectorStatus;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.enums.WasteReportStatus;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.collector.CollectionRequestRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.collector.CollectionTrackingRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.collector.CollectorRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.waste.WasteReportRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.service.EnterpriseAssignmentService;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.service.EnterpriseRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor

public class EnterpriseAssignmentServiceImpl implements EnterpriseAssignmentService {
    private final CollectionRequestRepository collectionRequestRepository;
    private final CollectorRepository collectorRepository;
    private final CollectionTrackingRepository collectionTrackingRepository;
    private final WasteReportRepository wasteReportRepository;
    private final EnterpriseRequestService enterpriseRequestService;

    @Override
    @Transactional
    public AssignCollectorResponse assignCollector(Integer enterpriseId, Integer requestId, Integer collectorId) {
        if (enterpriseId == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User hiện tại không phải Enterprise");
        }
        if (requestId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Thiếu request_id");
        }
        if (collectorId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Thiếu collector_id");
        }

        var collector = collectorRepository.findById(collectorId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Collector không tồn tại"));

        if (collector.getEnterprise() == null || collector.getEnterprise().getId() == null
                || !collector.getEnterprise().getId().equals(enterpriseId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Collector không thuộc doanh nghiệp");
        }

        if (collector.getStatus() == null
                || (collector.getStatus() != CollectorStatus.ACTIVE
                        && collector.getStatus() != CollectorStatus.AVAILABLE)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Collector không ở trạng thái active hoặc available");
        }

        LocalDateTime now = LocalDateTime.now();
        int updated = collectionRequestRepository.assignCollector(requestId, collectorId, enterpriseId);
        if (updated == 0) {
            CollectionRequest request = collectionRequestRepository.findById(requestId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "Collection Request không tồn tại"));
            if (request.getEnterprise() == null || request.getEnterprise().getId() == null
                    || !request.getEnterprise().getId().equals(enterpriseId)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Collection Request không thuộc doanh nghiệp");
            }
            if (request.getCollector() != null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Collection Request đã được gán collector");
            }
            if (request.getStatus() != CollectionRequestStatus.ACCEPTED_ENTERPRISE) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Collection Request không ở trạng thái hợp lệ để phân công (ACCEPTED_ENTERPRISE)");
            }
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Không thể phân công Collection Request");
        }

        CollectionRequest request = collectionRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Collection Request không tồn tại"));

        Integer collectionRequestId = request.getId();
        WasteReport report = request.getReport();
        if (report == null) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Collection Request thiếu report_id, không thể cập nhật WasteReport");
        }
        report.setStatus(WasteReportStatus.ASSIGNED);
        report.setUpdatedAt(now);
        wasteReportRepository.saveAndFlush(report);

        CollectionTracking tracking = new CollectionTracking();
        tracking.setCollectionRequest(collectionRequestRepository.getReferenceById(collectionRequestId));
        tracking.setCollector(collector);
        tracking.setAction("assigned");
        tracking.setNote("Enterprise assigned collector");
        tracking.setCreatedAt(now);
        collectionTrackingRepository.save(tracking);

        return AssignCollectorResponse.builder()
                .collectionRequestId(collectionRequestId)
                .collectorId(collector.getId())
                .status("assigned")
                .assignedAt(now)
                .build();
    }

    @Override
    @Transactional
    public AssignCollectorResponse assignCollectorByReportCode(Integer enterpriseId, String reportCode, Integer collectorId) {
        if (enterpriseId == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User hiện tại không phải Enterprise");
        }
        if (reportCode == null || reportCode.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Thiếu report_code");
        }
        if (collectorId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Thiếu collector_id");
        }
        var collector = collectorRepository.findById(collectorId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Collector không tồn tại"));
        if (collector.getEnterprise() == null || collector.getEnterprise().getId() == null
                || !collector.getEnterprise().getId().equals(enterpriseId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Collector không thuộc doanh nghiệp");
        }
        if (collector.getStatus() == null
                || (collector.getStatus() != CollectorStatus.ACTIVE
                && collector.getStatus() != CollectorStatus.AVAILABLE)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Collector không ở trạng thái active hoặc available");
        }
        var report = wasteReportRepository.findByReportCode(reportCode)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Waste Report không tồn tại"));
        var existing = collectionRequestRepository.findByReport_Id(report.getId());
        Integer reqId;
        if (existing.isEmpty()) {
            reqId = enterpriseRequestService.acceptWasteReport(enterpriseId, reportCode);
        } else {
            reqId = existing.get().getId();
        }
        return assignCollector(enterpriseId, reqId, collectorId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EligibleCollectorResponse> findEligibleCollectors(Integer enterpriseId, Integer requestId, Double radiusKm) {
        if (enterpriseId == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User hiện tại không phải Enterprise");
        }
        if (requestId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Thiếu request_id");
        }

        CollectionRequest request = collectionRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Collection Request không tồn tại"));

        if (request.getEnterprise() == null || request.getEnterprise().getId() == null
                || !request.getEnterprise().getId().equals(enterpriseId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Collection Request không thuộc doanh nghiệp");
        }
        if (request.getStatus() != CollectionRequestStatus.ACCEPTED_ENTERPRISE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Chỉ hỗ trợ tìm collector khi request ở trạng thái ACCEPTED_ENTERPRISE");
        }

        var collectors = collectorRepository.findByEnterprise_IdOrderByCreatedAtDesc(enterpriseId);
        LocalDateTime now = LocalDateTime.now();
        return collectors.stream()
                .filter(c -> c.getStatus() != null && (c.getStatus() == CollectorStatus.ACTIVE || c.getStatus() == CollectorStatus.AVAILABLE))
                .filter(c -> c.getStatus() != CollectorStatus.SUSPEND)
                .map(c -> {
                    boolean online = c.getLastLocationUpdate() != null && Duration.between(c.getLastLocationUpdate(), now).toMinutes() <= 15;
                    int active = (int) (
                            collectionRequestRepository.countByCollector_IdAndStatus(c.getId(), CollectionRequestStatus.ASSIGNED)
                                    + collectionRequestRepository.countByCollector_IdAndStatus(c.getId(), CollectionRequestStatus.ACCEPTED_COLLECTOR)
                                    + collectionRequestRepository.countByCollector_IdAndStatus(c.getId(), CollectionRequestStatus.ON_THE_WAY)
                    );
                    return EligibleCollectorResponse.builder()
                            .id(c.getId())
                            .fullName(c.getFullName())
                            .status(c.getStatus() != null ? c.getStatus().name() : null)
                            .distanceKm(null)
                            .online(online)
                            .activeTaskCount(active)
                            .build();
                })
                .sorted((a, b) -> {
                    int onlineCmp = Boolean.compare(Boolean.TRUE.equals(b.getOnline()), Boolean.TRUE.equals(a.getOnline()));
                    if (onlineCmp != 0) return onlineCmp;
                    return Integer.compare(a.getActiveTaskCount(), b.getActiveTaskCount());
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public RequestPreviewResponse getRequestPreview(Integer enterpriseId, Integer requestId) {
        if (enterpriseId == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User hiện tại không phải Enterprise");
        }
        if (requestId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Thiếu request_id");
        }
        CollectionRequest request = collectionRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Collection Request không tồn tại"));
        if (request.getEnterprise() == null || request.getEnterprise().getId() == null
                || !request.getEnterprise().getId().equals(enterpriseId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Collection Request không thuộc doanh nghiệp");
        }
        WasteReport report = request.getReport();
        LocalDateTime created = request.getCreatedAt();
        int sla = computeSlaHours(report != null ? report.getWasteType() : null);
        LocalDateTime due = created != null ? created.plusHours(sla) : null;
        Long remaining = null;
        if (due != null) {
            remaining = Duration.between(LocalDateTime.now(), due).toHours();
        }
        return RequestPreviewResponse.builder()
                .id(request.getId())
                .requestCode(request.getRequestCode())
                .latitude(report != null ? report.getLatitude() : null)
                .longitude(report != null ? report.getLongitude() : null)
                .createdAt(created)
                .wasteType(report != null ? report.getWasteType() : null)
                .slaHours(sla)
                .dueAt(due)
                .hoursRemaining(remaining)
                .priority("FCFS")
                .build();
    }

    private int computeSlaHours(String wasteType) {
        if (wasteType == null) {
            return 72;
        }
        String code = wasteType.trim().toUpperCase();
        return switch (code) {
            case "HAZARDOUS" -> 24;
            case "HOUSEHOLD" -> 48;
            case "RECYCLABLE" -> 72;
            default -> 72;
        };
    }

}
