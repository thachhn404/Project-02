package com.team2.Crowdsourced_Waste_Collection_Recycling_System.service.impl;

import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response.AssignCollectorResponse;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.CollectionRequest;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.CollectionTracking;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.CollectionRequestRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.CollectionTrackingRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.CollectorRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.service.EnterpriseAssignmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor

public class EnterpriseAssignmentServiceImpl implements EnterpriseAssignmentService {
    private final CollectionRequestRepository collectionRequestRepository;
    private final CollectorRepository collectorRepository;
    private final CollectionTrackingRepository collectionTrackingRepository;

    @Override
    @Transactional

    public AssignCollectorResponse assignCollector(Integer enterpriseId, Integer collectionRequestId, Integer collectorId) {
        if (enterpriseId == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User hiện tại không phải Enterprise");
        }
        if (collectionRequestId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Thiếu collection_request_id");
        }
        if (collectorId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Thiếu collector_id");
        }

        CollectionRequest collectionRequest = collectionRequestRepository.findById(collectionRequestId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Collection Request không tồn tại"));

        if (collectionRequest.getEnterprise() == null || collectionRequest.getEnterprise().getId() == null
                || !collectionRequest.getEnterprise().getId().equals(enterpriseId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Collection Request không thuộc doanh nghiệp");
        }

        if (collectionRequest.getCollector() != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Collection Request đã được gán collector");
        }

        if (collectionRequest.getStatus() == null || !"pending".equalsIgnoreCase(collectionRequest.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Collection Request chưa ở trạng thái pending");
        }

        var collector = collectorRepository.findById(collectorId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Collector không tồn tại"));

        if (collector.getEnterprise() == null || collector.getEnterprise().getId() == null
                || !collector.getEnterprise().getId().equals(enterpriseId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Collector không thuộc doanh nghiệp");
        }

        if (collector.getStatus() == null
                || (!"active".equalsIgnoreCase(collector.getStatus()) && !"available".equalsIgnoreCase(collector.getStatus()))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Collector không ở trạng thái active");
        }

        LocalDateTime now = LocalDateTime.now();
        collectionRequest.setCollector(collector);
        collectionRequest.setStatus("assigned");
        collectionRequest.setAssignedAt(now);
        collectionRequest.setUpdatedAt(now);
        collectionRequestRepository.save(collectionRequest);

        CollectionTracking tracking = new CollectionTracking();
        tracking.setCollectionRequest(collectionRequest);
        tracking.setCollector(collector);
        tracking.setAction("assigned");
        tracking.setNote("Enterprise assigned collector");
        tracking.setCreatedAt(now);
        collectionTrackingRepository.save(tracking);

        return AssignCollectorResponse.builder()
                .collectionRequestId(collectionRequest.getId())
                .collectorId(collector.getId())
                .status(collectionRequest.getStatus())
                .assignedAt(collectionRequest.getAssignedAt())
                .build();
    }
}
