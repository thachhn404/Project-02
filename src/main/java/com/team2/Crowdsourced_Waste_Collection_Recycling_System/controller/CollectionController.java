package com.team2.Crowdsourced_Waste_Collection_Recycling_System.controller;

import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.CollectionRequest;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.CollectionRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller dành cho Người thu gom (Collector).
 * Sử dụng @PreAuthorize để đảm bảo chỉ Collector mới có quyền truy cập.
 */
@RestController
@RequestMapping("/api/collector/collections")
@RequiredArgsConstructor
public class CollectionController {

    private final CollectionRequestRepository collectionRequestRepository;

    /**
     * Lấy danh sách các yêu cầu thu gom được giao cho Collector này.
     */
    @GetMapping("/my-tasks")
    @PreAuthorize("hasRole('COLLECTOR')")
    public ResponseEntity<List<CollectionRequest>> getMyTasks() {
        // Trong thực tế sẽ lấy collectorId từ SecurityContext
        return ResponseEntity.ok(collectionRequestRepository.findAll());
    }

    /**
     * Cập nhật trạng thái thu gom (ví dụ: đang thu gom, đã hoàn thành).
     */
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('COLLECTOR')")
    public ResponseEntity<CollectionRequest> updateStatus(@PathVariable Integer id, @RequestParam String status) {
        return collectionRequestRepository.findById(id)
                .map(request -> {
                    request.setStatus(status);
                    return ResponseEntity.ok(collectionRequestRepository.save(request));
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
