package com.team2.Crowdsourced_Waste_Collection_Recycling_System.service.impl;

import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.CollectionRequest;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.CollectionTracking;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.Collector;
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

    @Override
    @Transactional

    public void acceptTask(Integer requestId, Integer collectorId) {
        CollectionRequest request = getValidRequest(requestId, collectorId, "assigned");
        logTracking(request, collectorId, "accepted", "Collector accepted task");
    }

    @Override
    @Transactional

    public void startTask(Integer requestId, Integer collectorId) {
        LocalDateTime now = LocalDateTime.now();
        // Validate ownership + status hiện tại trước khi cập nhật
        CollectionRequest request = getValidRequest(requestId, collectorId, "assigned");

        request.setStatus("on_the_way");
        request.setStartedAt(now);
        request.setUpdatedAt(now);
        collectionRequestRepository.save(request);

        logTracking(request, collectorId, "started", "Collector started moving");
    }

    @Override
    @Transactional

    public void rejectTask(Integer requestId, Integer collectorId, String reason) {
        if (reason == null || reason.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Lý do từ chối là bắt buộc");
        }

        // Validate ownership + status hiện tại trước khi cập nhật
        CollectionRequest request = getValidRequest(requestId, collectorId, "assigned");
        LocalDateTime now = LocalDateTime.now();

        request.setStatus("accepted");
        request.setRejectionReason(reason);
        request.setCollector(null);
        request.setUpdatedAt(now);
        collectionRequestRepository.save(request);

        logTracking(request, collectorId, "rejected", "Collector rejected task: " + reason);
    }

    @Override
    @Transactional

    public void completeTask(Integer requestId, Integer collectorId) {
        // Validate ownership + status hiện tại trước khi cập nhật
        CollectionRequest request = getValidRequest(requestId, collectorId, "on_the_way");
        LocalDateTime now = LocalDateTime.now();

        request.setStatus("collected");
        request.setCollectedAt(now);
        request.setUpdatedAt(now);
        collectionRequestRepository.save(request);

        logTracking(request, collectorId, "collected", "Collector completed task");
    }



    private CollectionRequest getValidRequest(Integer requestId, Integer collectorId, String expectedStatus) {
        CollectionRequest request = collectionRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Collection Request không tồn tại"));

        if (request.getCollector() == null || !request.getCollector().getId().equals(collectorId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Collection Request không thuộc về bạn");
        }

        if (!expectedStatus.equalsIgnoreCase(request.getStatus())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    String.format(
                            "Trạng thái không hợp lệ để thực hiện hành động này. Mong đợi '%s' nhưng thực tế là '%s'.",
                            expectedStatus,
                            request.getStatus()
                    )
            );
        }
        return request;
    }


    private void logTracking(CollectionRequest request, Integer collectorId, String action, String note) {
        CollectionTracking tracking = new CollectionTracking();
        Collector collector = collectorRepository.getReferenceById(collectorId);

        tracking.setCollectionRequest(request);
        tracking.setCollector(collector);
        tracking.setAction(action);
        tracking.setNote(note);
        tracking.setCreatedAt(LocalDateTime.now());
        collectionTrackingRepository.save(tracking);
    }
}

