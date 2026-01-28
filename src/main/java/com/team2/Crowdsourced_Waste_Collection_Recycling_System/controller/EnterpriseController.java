package com.team2.Crowdsourced_Waste_Collection_Recycling_System.controller;

import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.CollectionRequest;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.CollectionRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller dành cho Doanh nghiệp tái chế (Enterprise).
 */
@RestController
@RequestMapping("/api/enterprise/requests")
@RequiredArgsConstructor
public class EnterpriseController {

    private final CollectionRequestRepository collectionRequestRepository;

    /**
     * Xem tất cả yêu cầu thu gom thuộc về doanh nghiệp này.
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ENTERPRISE', 'ENTERPRISE_ADMIN')")
    public ResponseEntity<List<CollectionRequest>> getAllRequests() {
        return ResponseEntity.ok(collectionRequestRepository.findAll());
    }

    /**
     * Giao một yêu cầu thu gom cho một nhân viên (Collector).
     * Yêu cầu quyền ENTERPRISE_ADMIN để thực hiện việc điều phối.
     */
    @PostMapping("/{id}/assign")
    @PreAuthorize("hasAuthority('ASSIGN_COLLECTOR')")
    public ResponseEntity<CollectionRequest> assignCollector(@PathVariable Integer id, @RequestParam Integer collectorId) {
        // Logic gán collector...
        return collectionRequestRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
