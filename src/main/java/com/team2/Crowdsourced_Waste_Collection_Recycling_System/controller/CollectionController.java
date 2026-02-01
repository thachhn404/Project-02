package com.team2.Crowdsourced_Waste_Collection_Recycling_System.controller;

import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response.ApiResponse;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.CollectionRequestRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.service.CollectorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

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

    /**
     * Xem danh sách task đã được gán cho collector hiện tại.
     * Có thể lọc theo status (assigned / on_the_way / collected ...).
     */
    @GetMapping("/tasks")
    @PreAuthorize("hasRole('COLLECTOR')")
    public ApiResponse<List<CollectionRequestRepository.CollectorTaskView>> getMyTasks(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(value = "status", required = false) String status
    ) {
        Integer collectorId = extractCollectorId(jwt);
        List<CollectionRequestRepository.CollectorTaskView> result = (status == null || status.isBlank())
                ? collectionRequestRepository.findActiveTasksForCollector(collectorId)
                : collectionRequestRepository.findTasksForCollectorByStatus(collectorId, status);
        return ApiResponse.<List<CollectionRequestRepository.CollectorTaskView>>builder().result(result).build();
    }

    /**
     * Collector bắt đầu di chuyển: assigned -> on_the_way.
     * Nếu task không thuộc collector hiện tại hoặc sai trạng thái sẽ trả lỗi.
     */
    @PostMapping("/{id}/start")
    @PreAuthorize("hasRole('COLLECTOR')")
    public ApiResponse<Void> startTask(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Integer id
    ) {
        Integer collectorId = extractCollectorId(jwt);
        collectorService.startTask(id, collectorId);
        return ApiResponse.<Void>builder().message("OK").build();
    }

    /**
     * Collector chấp nhận nhiệm vụ (status vẫn giữ assigned, chỉ ghi audit log).
     */
    @PostMapping("/{id}/accept")
    @PreAuthorize("hasRole('COLLECTOR')")
    public ApiResponse<Void> acceptTask(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Integer id
    ) {
        Integer collectorId = extractCollectorId(jwt);
        collectorService.acceptTask(id, collectorId);
        return ApiResponse.<Void>builder().message("OK").build();
    }

    /**
     * Collector từ chối nhiệm vụ (chỉ khi trạng thái assigned).
     * Body bắt buộc: { "reason": "..." }
     */
    @PostMapping("/{id}/reject")
    @PreAuthorize("hasRole('COLLECTOR')")
    public ApiResponse<Void> rejectTask(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Integer id,
            @RequestBody(required = false) java.util.Map<String, String> body
    ) {
        Integer collectorId = extractCollectorId(jwt);
        String reason = body == null ? null : body.get("reason");
        collectorService.rejectTask(id, collectorId, reason);
        return ApiResponse.<Void>builder().message("OK").build();
    }

    /**
     * Collector hoàn thành thu gom: on_the_way -> collected.
     */
    @PostMapping("/{id}/complete")
    @PreAuthorize("hasRole('COLLECTOR')")
    public ApiResponse<Void> completeTask(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Integer id
    ) {
        Integer collectorId = extractCollectorId(jwt);
        collectorService.completeTask(id, collectorId);
        return ApiResponse.<Void>builder().message("OK").build();
    }

    /**
     * Lấy collectorId từ JWT claim (do hệ thống cấp khi đăng nhập).
     */
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
