package com.team2.Crowdsourced_Waste_Collection_Recycling_System.controller;

import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.request.RejectTaskRequest;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response.ApiResponse;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response.CollectionRequestActionResponse;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.CollectionRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import com.team2.Crowdsourced_Waste_Collection_Recycling_System.service.CollectorService;

import java.time.LocalDateTime;

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
    private final CollectorService collectorService;

    @GetMapping("/tasks")
    @PreAuthorize("hasRole('COLLECTOR')")
    public ApiResponse<List<CollectionRequestRepository.CollectorTaskView>> getTasks(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "all", required = false, defaultValue = "false") boolean all
    ) {
        Integer collectorId = extractCollectorId(jwt);
        List<CollectionRequestRepository.CollectorTaskView> tasks;
        if (all) {
            tasks = collectionRequestRepository.findTasksForCollector(collectorId);
        } else if (status != null && !status.isBlank()) {
            tasks = collectionRequestRepository.findTasksForCollectorByStatus(collectorId, status);
        } else {
            tasks = collectionRequestRepository.findActiveTasksForCollector(collectorId);
        }
        return ApiResponse.<List<CollectionRequestRepository.CollectorTaskView>>builder().result(tasks).build();
    }

    @PostMapping("/{id}/accept")
    @PreAuthorize("hasRole('COLLECTOR')")
    public ApiResponse<CollectionRequestActionResponse> acceptTask(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Integer id
    ) {
        Integer collectorId = extractCollectorId(jwt);
        collectorService.acceptTask(id, collectorId);
        return ApiResponse.<CollectionRequestActionResponse>builder()
                .result(CollectionRequestActionResponse.builder()
                        .collectionRequestId(id)
                        .status("accepted_collector")
                        .actionAt(LocalDateTime.now())
                        .build())
                .build();
    }

    @PostMapping("/{id}/start")
    @PreAuthorize("hasRole('COLLECTOR')")
    public ApiResponse<CollectionRequestActionResponse> startTask(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Integer id
    ) {
        Integer collectorId = extractCollectorId(jwt);
        collectorService.startTask(id, collectorId);
        return ApiResponse.<CollectionRequestActionResponse>builder()
                .result(CollectionRequestActionResponse.builder()
                        .collectionRequestId(id)
                        .status("on_the_way")
                        .actionAt(LocalDateTime.now())
                        .build())
                .build();
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasRole('COLLECTOR')")
    public ApiResponse<CollectionRequestActionResponse> rejectTask(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Integer id,
            @RequestBody(required = false) RejectTaskRequest request
    ) {
        Integer collectorId = extractCollectorId(jwt);
        String reason = request != null ? request.getReason() : null;
        collectorService.rejectTask(id, collectorId, reason);
        return ApiResponse.<CollectionRequestActionResponse>builder()
                .result(CollectionRequestActionResponse.builder()
                        .collectionRequestId(id)
                        .status("accepted_enterprise")
                        .actionAt(LocalDateTime.now())
                        .build())
                .build();
    }

    @PostMapping("/{id}/complete")
    @PreAuthorize("hasRole('COLLECTOR')")
    public ApiResponse<CollectionRequestActionResponse> completeTask(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Integer id
    ) {
        Integer collectorId = extractCollectorId(jwt);
        collectorService.completeTask(id, collectorId);
        return ApiResponse.<CollectionRequestActionResponse>builder()
                .result(CollectionRequestActionResponse.builder()
                        .collectionRequestId(id)
                        .status("collected")
                        .actionAt(LocalDateTime.now())
                        .build())
                .build();
    }

    private Integer extractCollectorId(Jwt jwt) {
        if (jwt == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Thiếu token");
        }
        Object value = jwt.getClaims().get("collectorId");
        if (value == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User hiện tại không phải Collector");
        }
        if (value instanceof Number number) {
            return number.intValue();
        }
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "collectorId không hợp lệ");
    }
}
