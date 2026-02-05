package com.team2.Crowdsourced_Waste_Collection_Recycling_System.service.impl;

import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.CollectionRequest;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.enums.CollectionRequestStatus;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.CollectionTracking;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.Collector;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.collector.CollectionRequestRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.collector.CollectionTrackingRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.collector.CollectorRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.service.CollectorService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CollectorServiceImpl implements CollectorService {
    private final CollectionRequestRepository collectionRequestRepository;
    private final CollectionTrackingRepository collectionTrackingRepository;
    private final CollectorRepository collectorRepository;

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
        logTracking(requestId, collectorId, "collected", "Collector completed task");
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
