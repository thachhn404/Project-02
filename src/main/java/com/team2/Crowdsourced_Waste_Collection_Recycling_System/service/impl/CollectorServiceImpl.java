package com.team2.Crowdsourced_Waste_Collection_Recycling_System.service.impl;

import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response.CollectorMonthlyCompletedCountResponse;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response.CollectorPerformanceStatsResponse;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response.CollectorTaskResponse;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response.CollectorWorkHistoryItemResponse;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.CollectionRequest;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.enums.CollectionRequestStatus;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.CollectionTracking;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.Collector;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.enums.WasteReportStatus;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.waste.WasteReportRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.collector.CollectionRequestRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.collector.CollectionTrackingRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.collector.CollectorRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.service.CollectorService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CollectorServiceImpl implements CollectorService {
    private final CollectionRequestRepository collectionRequestRepository;
    private final CollectionTrackingRepository collectionTrackingRepository;
    private final CollectorRepository collectorRepository;
    private final WasteReportRepository wasteReportRepository;

    @Override
    public Page<CollectorTaskResponse> getTasks(Integer collectorId, String status, boolean all, Pageable pageable) {
        Page<CollectionRequestRepository.CollectorTaskView> tasks;
        if (all) {
            tasks = collectionRequestRepository.findTasksForCollector(collectorId, pageable);
        } else if (status != null && !status.isBlank()) {
            tasks = collectionRequestRepository.findTasksForCollectorByStatus(collectorId, status, pageable);
        } else {
            tasks = collectionRequestRepository.findActiveTasksForCollector(collectorId, pageable);
        }
        return tasks.map(t -> CollectorTaskResponse.builder()
                .id(t.getId())
                .requestCode(t.getRequestCode())
                .status(t.getStatus())
                .assignedAt(t.getAssignedAt())
                .createdAt(t.getCreatedAt())
                .updatedAt(t.getUpdatedAt())
                .build());
    }

    @Override
    public Page<CollectorWorkHistoryItemResponse> getWorkHistory(Integer collectorId, String status, Pageable pageable) {
        Page<CollectionRequestRepository.CollectorWorkHistoryView> rows;
        if (status != null && !status.isBlank()) {
            rows = collectionRequestRepository.findWorkHistoryForCollectorByStatus(collectorId, status, pageable);
        } else {
            rows = collectionRequestRepository.findWorkHistoryForCollector(collectorId, pageable);
        }
        return rows.map(row -> CollectorWorkHistoryItemResponse.builder()
                .collectionRequestId(row.getId())
                .requestCode(row.getRequestCode())
                .status(row.getStatus())
                .address(row.getAddress())
                .wasteTypeCode(row.getWasteTypeCode())
                .wasteTypeName(row.getWasteTypeName())
                .enterpriseId(row.getEnterpriseId())
                .enterpriseName(row.getEnterpriseName())
                .startedAt(row.getStartedAt())
                .collectedAt(row.getCollectedAt())
                .completedAt(row.getCompletedAt())
                .updatedAt(row.getUpdatedAt())
                .build());
    }

    @Override
    public CollectorPerformanceStatsResponse getStats(Integer collectorId, Integer year) {
        int selectedYear = year != null ? year : LocalDate.now().getYear();

        long totalCompleted = collectionRequestRepository.countByCollector_IdAndStatus(
                collectorId, CollectionRequestStatus.COMPLETED) + collectionRequestRepository.countByCollector_IdAndStatus(
                collectorId, CollectionRequestStatus.COLLECTED);
        List<CollectionRequestRepository.CollectorMonthlyCompletedCountView> rows =
                collectionRequestRepository.countCompletedByMonth(collectorId, selectedYear);

        List<CollectorMonthlyCompletedCountResponse> completedByMonth = new ArrayList<>();
        for (var row : rows) {
            completedByMonth.add(CollectorMonthlyCompletedCountResponse.builder()
                    .year(row.getYear())
                    .month(row.getMonth())
                    .total(row.getTotal())
                    .build());
        }

        return CollectorPerformanceStatsResponse.builder()
                .totalCompleted(totalCompleted)
                .year(selectedYear)
                .completedByMonth(completedByMonth)
                .build();
    }

    @Override
    @Transactional
    public void updateStatus(Integer requestId, Integer collectorId, String statusStr) {
        if ("ON_THE_WAY".equalsIgnoreCase(statusStr)) {
            startTask(requestId, collectorId);
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid status. Only 'ON_THE_WAY' is allowed.");
        }
    }

    @Override
    @Transactional
    /**
     * assigned -> accepted_collector.
     */
    public void acceptTask(Integer requestId, Integer collectorId) {
        LocalDateTime now = LocalDateTime.now();
        int updated = collectionRequestRepository.acceptTask(requestId, collectorId, now);
        if (updated == 0) {
            throwUpdateStatusError(requestId, collectorId, CollectionRequestStatus.ASSIGNED);
        }
        updateWasteReportStatusIfPresent(requestId, WasteReportStatus.ACCEPTED_COLLECTOR, now);
        logTracking(requestId, collectorId, "accepted", "Collector accepted task");
    }

    @Override
    @Transactional
    /**
     * accepted_collector -> on_the_way.
     */
    public void startTask(Integer requestId, Integer collectorId) {
        LocalDateTime now = LocalDateTime.now();
        int updated = collectionRequestRepository.updateStatusIfMatch(
                requestId, collectorId, CollectionRequestStatus.ACCEPTED_COLLECTOR, CollectionRequestStatus.ON_THE_WAY, now);
        if (updated == 0) {
            throwUpdateStatusError(requestId, collectorId, CollectionRequestStatus.ACCEPTED_COLLECTOR);
        }
        updateWasteReportStatusIfPresent(requestId, WasteReportStatus.ON_THE_WAY, now);
        logTracking(requestId, collectorId, "started", "Collector started moving");
    }

    @Override
    @Transactional
    /**
     * Từ chối task (chỉ khi assigned):
     * - status -> accepted_enterprise
     * - unassign collector
     */
    public void rejectTask(Integer requestId, Integer collectorId, String reason) {
        if (reason == null || reason.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Lý do từ chối là bắt buộc");
        }
        LocalDateTime now = LocalDateTime.now();
        int updated = collectionRequestRepository.rejectTask(requestId, collectorId, reason);
        if (updated == 0) {
            throwRejectError(requestId, collectorId);
        }
        updateWasteReportStatusIfPresent(requestId, WasteReportStatus.ACCEPTED_ENTERPRISE, now);
        logTracking(requestId, collectorId, "rejected", "Collector rejected task: " + reason);
    }

    @Override
    @Transactional
    /**
     * on_the_way -> collected
     */
    public void completeTask(Integer requestId, Integer collectorId) {
        LocalDateTime now = LocalDateTime.now();
        int updated = collectionRequestRepository.completeTask(requestId, collectorId, now);
        if (updated == 0) {
            throwUpdateStatusError(requestId, collectorId, CollectionRequestStatus.ON_THE_WAY);
        }
        updateWasteReportStatusIfPresent(requestId, WasteReportStatus.COLLECTED, now);
        logTracking(requestId, collectorId, "collected", "Collector completed task");
    }

    private void updateWasteReportStatusIfPresent(Integer requestId, WasteReportStatus newStatus, LocalDateTime now) {
        CollectionRequest request = collectionRequestRepository.findById(requestId).orElse(null);
        if (request == null || request.getReport() == null) {
            return;
        }
        var report = request.getReport();
        report.setStatus(newStatus);
        report.setUpdatedAt(now);
        wasteReportRepository.save(report);
    }

    private CollectionRequest getValidRequest(Integer requestId, Integer collectorId, CollectionRequestStatus expectedStatus) {
        CollectionRequest request = collectionRequestRepository.findById(requestId)
                .orElseThrow(
                        () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Collection Request không tồn tại"));

        if (request.getCollector() == null || !request.getCollector().getId().equals(collectorId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Collection Request không thuộc về bạn");
        }

        if (request.getStatus() != expectedStatus) {
            String message = String.format("Trạng thái không hợp lệ. Mong đợi '%s' nhưng thực tế là '%s'.",
                    expectedStatus, request.getStatus());
            if (request.getStatus() == CollectionRequestStatus.ON_THE_WAY
                    && expectedStatus == CollectionRequestStatus.ASSIGNED) {
                message += " Không thể từ chối khi đã bắt đầu di chuyển.";
            }
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
        }
        return request;
    }

    private void logTracking(Integer requestId, Integer collectorId, String action, String note) {
        CollectionTracking tracking = new CollectionTracking();
        Collector collector = collectorRepository.getReferenceById(collectorId);

        tracking.setCollectionRequest(collectionRequestRepository.getReferenceById(requestId));
        tracking.setCollector(collector);
        tracking.setAction(action);
        tracking.setNote(note);
        tracking.setCreatedAt(LocalDateTime.now());
        collectionTrackingRepository.save(tracking);
    }

    private void throwUpdateStatusError(Integer requestId, Integer collectorId, CollectionRequestStatus expectedStatus) {
        CollectionRequest request = collectionRequestRepository.findById(requestId)
                .orElseThrow(
                        () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Collection Request không tồn tại"));
        if (request.getCollector() == null || request.getCollector().getId() == null
                || !request.getCollector().getId().equals(collectorId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Collection Request không thuộc về bạn");
        }
        if (expectedStatus != request.getStatus()) {
            String message = String.format("Trạng thái không hợp lệ. Mong đợi '%s' nhưng thực tế là '%s'.",
                    expectedStatus, request.getStatus());
            if ((expectedStatus == CollectionRequestStatus.ASSIGNED || expectedStatus == CollectionRequestStatus.ACCEPTED_COLLECTOR)
                    && request.getStatus() == CollectionRequestStatus.ON_THE_WAY) {
                message += " Không thể từ chối khi đã bắt đầu di chuyển.";
            }
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
        }
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Không thể cập nhật trạng thái Collection Request");
    }

    private void throwRejectError(Integer requestId, Integer collectorId) {
        CollectionRequest request = collectionRequestRepository.findById(requestId)
                .orElseThrow(
                        () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Collection Request không tồn tại"));
        if (request.getCollector() == null || request.getCollector().getId() == null
                || !request.getCollector().getId().equals(collectorId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Collection Request không thuộc về bạn");
        }
        if (request.getStatus() != CollectionRequestStatus.ASSIGNED) {
            String message = String.format("Trạng thái không hợp lệ. Mong đợi '%s' nhưng thực tế là '%s'.", "ASSIGNED",
                    request.getStatus());
            if (request.getStatus() == CollectionRequestStatus.ON_THE_WAY) {
                message += " Không thể từ chối khi đã bắt đầu di chuyển.";
            }
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
        }
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Không thể từ chối nhiệm vụ");
    }

}
