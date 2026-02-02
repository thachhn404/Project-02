package com.team2.Crowdsourced_Waste_Collection_Recycling_System.service.impl;

import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.AuditLog;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.CollectionRequest;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.AuditLogRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.CollectionRequestRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.service.EnterpriseRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class EnterpriseRequestServiceImpl implements EnterpriseRequestService {
    private final CollectionRequestRepository collectionRequestRepository;
    private final AuditLogRepository auditLogRepository;

    @Override
    @Transactional
    public void acceptRequest(Integer enterpriseId, Integer collectionRequestId) {
        if (enterpriseId == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User hiện tại không phải Enterprise");
        }
        if (collectionRequestId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Thiếu collection_request_id");
        }

        int updated = collectionRequestRepository.acceptByEnterprise(collectionRequestId, enterpriseId);
        if (updated == 0) {
            CollectionRequest request = collectionRequestRepository.findById(collectionRequestId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Collection Request không tồn tại"));
            if (request.getEnterprise() == null || request.getEnterprise().getId() == null
                    || !request.getEnterprise().getId().equals(enterpriseId)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Collection Request không thuộc doanh nghiệp");
            }
            if (request.getCollector() != null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Collection Request đã được gán collector");
            }
            if (request.getStatus() == null || !"pending".equalsIgnoreCase(request.getStatus())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Collection Request không ở trạng thái pending");
            }
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Không thể accept Collection Request");
        }

        LocalDateTime now = LocalDateTime.now();
        AuditLog auditLog = new AuditLog();
        auditLog.setActorId(enterpriseId);
        auditLog.setActorRole("ENTERPRISE");
        auditLog.setAction("accept_enterprise");
        auditLog.setTargetType("COLLECTION_REQUEST");
        auditLog.setTargetId(collectionRequestId);
        auditLog.setMetadata(null);
        auditLog.setCreatedAt(now);
        auditLogRepository.save(auditLog);
    }
}
