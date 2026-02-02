package com.team2.Crowdsourced_Waste_Collection_Recycling_System.service.impl;

import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.CollectionRequest;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.CollectionTracking;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.Collector;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.AuditLog;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.AuditLogRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.CollectionRequestRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.CollectionTrackingRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.CollectorRepository;
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
    private final AuditLogRepository auditLogRepository;

    @Override
    @Transactional
    public void acceptTask(Integer requestId, Integer collectorId) {
        LocalDateTime now = LocalDateTime.now();
        int updated = collectionRequestRepository.acceptTask(requestId, collectorId, now);
        if (updated == 0) {
            throwUpdateStatusError(requestId, collectorId, "assigned");
        }
        logTracking(requestId, collectorId, "accepted", "Collector accepted task");
        saveAuditLog(collectorId, "COLLECTOR", "accept_collector", requestId, null, now);
    }

    @Override
    @Transactional
    public void startTask(Integer requestId, Integer collectorId) {
        LocalDateTime now = LocalDateTime.now();
        int updated = collectionRequestRepository.updateStatusIfMatch(
                requestId, collectorId, "accepted_collector", "on_the_way", now
        );
        if (updated == 0) {
            throwUpdateStatusError(requestId, collectorId, "accepted_collector");
        }
        logTracking(requestId, collectorId, "started", "Collector started moving");
        saveAuditLog(collectorId, "COLLECTOR", "start", requestId, null, now);
    }

    @Override
    @Transactional
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
        saveAuditLog(collectorId, "COLLECTOR", "reject", requestId, reason, now);
    }

    @Override
    @Transactional
    public void completeTask(Integer requestId, Integer collectorId) {
        LocalDateTime now = LocalDateTime.now();
        int updated = collectionRequestRepository.completeTask(requestId, collectorId, now);
        if (updated == 0) {
            throwUpdateStatusError(requestId, collectorId, "on_the_way");
        }
        logTracking(requestId, collectorId, "collected", "Collector completed task");
        saveAuditLog(collectorId, "COLLECTOR", "complete", requestId, null, now);
    }

    private CollectionRequest getValidRequest(Integer requestId, Integer collectorId, String expectedStatus) {
        CollectionRequest request = collectionRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Collection Request không tồn tại"));

        if (request.getCollector() == null || !request.getCollector().getId().equals(collectorId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Collection Request không thuộc về bạn");
        }

        if (!expectedStatus.equalsIgnoreCase(request.getStatus())) {
            String message = String.format("Trạng thái không hợp lệ. Mong đợi '%s' nhưng thực tế là '%s'.", expectedStatus, request.getStatus());
            if ("on_the_way".equalsIgnoreCase(request.getStatus()) && "assigned".equalsIgnoreCase(expectedStatus)) {
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

    private void throwUpdateStatusError(Integer requestId, Integer collectorId, String expectedStatus) {
        CollectionRequest request = collectionRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Collection Request không tồn tại"));
        if (request.getCollector() == null || request.getCollector().getId() == null
                || !request.getCollector().getId().equals(collectorId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Collection Request không thuộc về bạn");
        }
        if (!expectedStatus.equalsIgnoreCase(request.getStatus())) {
            String message = String.format("Trạng thái không hợp lệ. Mong đợi '%s' nhưng thực tế là '%s'.", expectedStatus, request.getStatus());
            if (("assigned".equalsIgnoreCase(expectedStatus) || "accepted_collector".equalsIgnoreCase(expectedStatus))
                    && "on_the_way".equalsIgnoreCase(request.getStatus())) {
                message += " Không thể từ chối khi đã bắt đầu di chuyển.";
            }
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
        }
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Không thể cập nhật trạng thái Collection Request");
    }

    private void throwRejectError(Integer requestId, Integer collectorId) {
        CollectionRequest request = collectionRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Collection Request không tồn tại"));
        if (request.getCollector() == null || request.getCollector().getId() == null
                || !request.getCollector().getId().equals(collectorId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Collection Request không thuộc về bạn");
        }
        if (!"assigned".equalsIgnoreCase(request.getStatus())) {
            String message = String.format("Trạng thái không hợp lệ. Mong đợi '%s' nhưng thực tế là '%s'.", "assigned", request.getStatus());
            if ("on_the_way".equalsIgnoreCase(request.getStatus())) {
                message += " Không thể từ chối khi đã bắt đầu di chuyển.";
            }
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
        }
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Không thể từ chối nhiệm vụ");
    }

    private void saveAuditLog(Integer actorId, String actorRole, String action, Integer requestId, String metadata, LocalDateTime time) {
        AuditLog auditLog = new AuditLog();
        auditLog.setActorId(actorId);
        auditLog.setActorRole(actorRole);
        auditLog.setAction(action);
        auditLog.setTargetType("COLLECTION_REQUEST");
        auditLog.setTargetId(requestId);
        auditLog.setMetadata(metadata);
        auditLog.setCreatedAt(time);
        auditLogRepository.save(auditLog);
    }
}

