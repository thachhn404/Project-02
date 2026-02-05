package com.team2.Crowdsourced_Waste_Collection_Recycling_System.service.impl;

import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response.AssignCollectorResponse;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.CollectionRequest;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.enums.CollectionRequestStatus;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.CollectionTracking;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.enums.CollectorStatus;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.collector.CollectionRequestRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.collector.CollectionTrackingRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.collector.CollectorRepository;
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
    public AssignCollectorResponse assignCollector(Integer enterpriseId, String requestCode, Integer collectorId) {
        if (enterpriseId == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User hiện tại không phải Enterprise");
        }
        if (requestCode == null || requestCode.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Thiếu request_code");
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
        int updated = collectionRequestRepository.assignCollectorByRequestCode(requestCode, collectorId, enterpriseId);
        if (updated == 0) {
            CollectionRequest request = collectionRequestRepository.findByRequestCode(requestCode)
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
                        "Collection Request không ở trạng thái hợp lệ để phân công");
            }
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Không thể phân công Collection Request");
        }

        Integer collectionRequestId = collectionRequestRepository.findByRequestCode(requestCode)
                .orElseThrow(
                        () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Collection Request không tồn tại"))
                .getId();

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
}
