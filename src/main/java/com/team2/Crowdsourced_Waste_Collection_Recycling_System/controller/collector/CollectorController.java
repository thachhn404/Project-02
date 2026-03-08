package com.team2.Crowdsourced_Waste_Collection_Recycling_System.controller.collector;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response.ApiResponse;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.request.UpdateCollectorStatusRequest;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.service.CollectorService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response.*;
import java.util.List;
import java.util.Map;
import java.math.BigDecimal;

@RestController
@RequestMapping("/api/collector")
@Tag(name = "Collector", description = "Endpoint dành cho người thu gom")
public class CollectorController {
    private final com.team2.Crowdsourced_Waste_Collection_Recycling_System.service.CollectorService collectorService;

    public CollectorController(CollectorService collectorService) {
        this.collectorService = collectorService;
    }

    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('COLLECTOR')")
    @Operation(summary = "Trang tổng quan Collector", description = "Thông tin tổng quan nhanh cho Collector")
    public ResponseEntity<String> getDashboard() {
        return ResponseEntity.ok("Hello Collector! This is your dashboard.");
    }

    @GetMapping("/stats/performance")
    @PreAuthorize("hasRole('COLLECTOR')")
    @Operation(summary = "Hiệu suất thu gom", description = "Thống kê số lượng thu gom theo tháng")
    public ApiResponse<CollectorPerformanceStatsResponse> getPerformanceStats(@AuthenticationPrincipal Jwt jwt, @RequestParam(required = false) Integer year) {
        Integer collectorId = extractCollectorId(jwt);
        return ApiResponse.<CollectorPerformanceStatsResponse>builder()
                .result(collectorService.getStats(collectorId, year))
                .build();
    }

    @GetMapping("/stats/waste-volume")
    @PreAuthorize("hasRole('COLLECTOR')")
    @Operation(summary = "Khối lượng rác thu gom", description = "Thống kê khối lượng rác theo tháng/quý")
    public ApiResponse<CollectorWasteVolumeStatsResponse> getWasteVolumeStats(@AuthenticationPrincipal Jwt jwt, @RequestParam(required = false) Integer year) {
        Integer collectorId = extractCollectorId(jwt);
        return ApiResponse.<CollectorWasteVolumeStatsResponse>builder()
                .result(collectorService.getWasteVolumeStats(collectorId, year))
                .build();
    }

    @GetMapping("/stats/waste-type")
    @PreAuthorize("hasRole('COLLECTOR')")
    @Operation(summary = "Thống kê theo loại rác", description = "Tổng khối lượng rác theo từng loại")
    public ApiResponse<Map<String, BigDecimal>> getWasteTypeStats(@AuthenticationPrincipal Jwt jwt) {
        Integer collectorId = extractCollectorId(jwt);
        return ApiResponse.<Map<String, BigDecimal>>builder()
                .result(collectorService.getWasteTypeStats(collectorId))
                .build();
    }

    @GetMapping("/stats/tasks")
    @PreAuthorize("hasRole('COLLECTOR')")
    @Operation(summary = "Thống kê trạng thái task", description = "Số lượng task theo từng trạng thái")
    public ApiResponse<List<CollectorTaskStatusCountResponse>> getTaskStatusCounts(@AuthenticationPrincipal Jwt jwt) {
        Integer collectorId = extractCollectorId(jwt);
        return ApiResponse.<List<CollectorTaskStatusCountResponse>>builder()
                .result(collectorService.getTaskStatusCounts(collectorId))
                .build();
    }

    @GetMapping("/history")
    @PreAuthorize("hasRole('COLLECTOR')")
    @Operation(summary = "Lịch sử thu gom", description = "Danh sách các task đã xử lý")
    public ApiResponse<List<CollectorWorkHistoryItemResponse>> getWorkHistory(@AuthenticationPrincipal Jwt jwt, @RequestParam(required = false) String status) {
        Integer collectorId = extractCollectorId(jwt);
        return ApiResponse.<List<CollectorWorkHistoryItemResponse>>builder()
                .result(collectorService.getWorkHistory(collectorId, status))
                .build();
    }

    @PatchMapping("/status")
    @PreAuthorize("hasRole('COLLECTOR')")
    @Operation(summary = "Cập nhật trạng thái", description = "Chuyển AVAILABLE/ACTIVE/INACTIVE")
    public ApiResponse<Void> updateMyStatus(@AuthenticationPrincipal Jwt jwt, @RequestBody UpdateCollectorStatusRequest request) {
        Integer collectorId = extractCollectorId(jwt);
        collectorService.updateAvailabilityStatus(collectorId, request != null ? request.getStatus() : null);
        return ApiResponse.<Void>builder().message("Updated").build();
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
