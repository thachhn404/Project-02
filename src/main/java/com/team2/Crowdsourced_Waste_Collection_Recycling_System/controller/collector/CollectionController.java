package com.team2.Crowdsourced_Waste_Collection_Recycling_System.controller.collector;

import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.request.CreateCollectorReportRequest;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.request.RejectTaskRequest;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.request.UpdateTaskStatusRequest;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response.*;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.service.CollectorReportService;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.service.CollectorService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
 
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
 
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Controller dành cho Người thu gom (Collector).
 * Sử dụng @PreAuthorize để đảm bảo chỉ Collector mới có quyền truy cập.
 */
@RestController
@RequestMapping("/api/collector/collections")
@RequiredArgsConstructor
@Validated
@Tag(name = "Collector Collections", description = "Nhiệm vụ và báo cáo của Collector")
public class CollectionController {
    private final CollectorService collectorService;
    private final CollectorReportService collectorReportService;

    /**
     * Lấy danh sách task của collector.
     * - Mặc định: chỉ hiển thị task active (assigned/accepted_collector/on_the_way)
     * - status: lọc theo trạng thái cụ thể
     * - all=true: lấy toàn bộ task
     */
    @GetMapping("/tasks")
    @PreAuthorize("hasRole('COLLECTOR')")
    @Operation(summary = "Danh sách task", description = "Hiển thị task active theo mặc định; hỗ trợ lọc status hoặc all=true")
    public ApiResponse<java.util.List<CollectorTaskResponse>> getTasks(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "all", required = false, defaultValue = "false") boolean all) {
        Integer collectorId = extractCollectorId(jwt);
        java.util.List<CollectorTaskResponse> tasks = collectorService.getTasks(collectorId, status, all);
        return ApiResponse.<java.util.List<CollectorTaskResponse>>builder().result(tasks).build();
    }

    @GetMapping("/work_history")
    @PreAuthorize("hasRole('COLLECTOR')")
    @Operation(summary = "Lịch sử công việc", description = "Liệt kê lịch sử làm việc, hỗ trợ lọc trạng thái")
    public ApiResponse<java.util.List<CollectorWorkHistoryItemResponse>> getWorkHistory(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(value = "status", required = false) String status) {
        Integer collectorId = extractCollectorId(jwt);
        java.util.List<CollectorWorkHistoryItemResponse> result = collectorService.getWorkHistory(collectorId, status);
        return ApiResponse.<java.util.List<CollectorWorkHistoryItemResponse>>builder()
                .result(result)
                .build();
    }

    @GetMapping("/stats")
    @PreAuthorize("hasRole('COLLECTOR')")
    @Operation(summary = "Thống kê hiệu suất", description = "Tổng hợp số liệu theo năm của Collector")
    public ApiResponse<CollectorPerformanceStatsResponse> getStats(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(value = "year", required = false) Integer year) {
        Integer collectorId = extractCollectorId(jwt);
        CollectorPerformanceStatsResponse stats = collectorService.getStats(collectorId, year);
        return ApiResponse.<CollectorPerformanceStatsResponse>builder()
                .result(stats)
                .build();
    }

    /**
     * Collector bắt đầu di chuyển: assigned -> on_the_way.
     */
    @PostMapping("/{requestId}/accept")
    @PreAuthorize("hasRole('COLLECTOR')")
    @Operation(summary = "Chấp nhận nhiệm vụ", description = "Chuyển ASSIGNED → ACCEPTED_COLLECTOR")
    public ApiResponse<CollectionRequestActionResponse> acceptTask(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Integer requestId) {
        Integer collectorId = extractCollectorId(jwt);
        collectorService.acceptTask(requestId, collectorId);
        return ApiResponse.<CollectionRequestActionResponse>builder()
                .result(CollectionRequestActionResponse.builder()
                        .collectionRequestId(requestId)
                        .status("accepted_collector")
                        .actionAt(LocalDateTime.now())
                        .build())
                .build();
    }

    /**
     * Collector bắt đầu di chuyển: accepted_collector -> on_the_way.
     */
    @PostMapping("/{requestId}/start")
    @PreAuthorize("hasRole('COLLECTOR')")
    @Operation(summary = "Bắt đầu di chuyển", description = "Chuyển ACCEPTED_COLLECTOR → ON_THE_WAY")
    public ApiResponse<CollectionRequestActionResponse> startTask(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Integer requestId) {
        Integer collectorId = extractCollectorId(jwt);
        collectorService.startTask(requestId, collectorId);
        return ApiResponse.<CollectionRequestActionResponse>builder()
                .result(CollectionRequestActionResponse.builder()
                        .collectionRequestId(requestId)
                        .status("on_the_way")
                        .actionAt(LocalDateTime.now())
                        .build())
                .build();
    }

    /**
     * Collector từ chối task (chỉ khi đang assigned):
     * - status -> accepted_enterprise
     * - unassign collector để enterprise phân công lại
     */
    @PostMapping("/{requestId}/reject")
    @PreAuthorize("hasRole('COLLECTOR')")
    @Operation(summary = "Từ chối nhiệm vụ", description = "Chỉ khi đang ASSIGNED; trả về ACCEPTED_ENTERPRISE")
    public ApiResponse<CollectionRequestActionResponse> rejectTask(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Integer requestId,
            @RequestBody(required = false) RejectTaskRequest request) {
        Integer collectorId = extractCollectorId(jwt);
        String reason = request != null ? request.getReason() : null;
        collectorService.rejectTask(requestId, collectorId, reason);
        return ApiResponse.<CollectionRequestActionResponse>builder()
                .result(CollectionRequestActionResponse.builder()
                        .collectionRequestId(requestId)
                        .status("accepted_enterprise")
                        .actionAt(LocalDateTime.now())
                        .build())
                .build();
    }

    /**
     * Collector xác nhận đã thu gom tại điểm:
     * - Cập nhật collection_request.status: on_the_way -> collected
     */
    @PostMapping("/{requestId}/collected")
    @PreAuthorize("hasRole('COLLECTOR')")
    @Operation(summary = "Đánh dấu đã thu gom", description = "Chuyển ON_THE_WAY → COLLECTED")
    public ApiResponse<CollectionRequestActionResponse> markCollected(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Integer requestId) {
        Integer collectorId = extractCollectorId(jwt);
        collectorService.completeTask(requestId, collectorId);
        return ApiResponse.<CollectionRequestActionResponse>builder()
                .result(CollectionRequestActionResponse.builder()
                        .collectionRequestId(requestId)
                        .status("collected")
                        .actionAt(LocalDateTime.now())
                        .build())
                .build();
    }

    /**
     * Cập nhật trạng thái nhiệm vụ (chỉ tiến về phía trước).
     * Hiện tại hỗ trợ: ON_THE_WAY.
     */
    @PatchMapping("/{requestId}/status")
    @PreAuthorize("hasRole('COLLECTOR')")
    @Operation(summary = "Cập nhật trạng thái", description = "Cập nhật trạng thái nhiệm vụ (chỉ tiến về phía trước)")
    public ApiResponse<CollectionRequestActionResponse> updateStatus(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Integer requestId,
            @RequestBody UpdateTaskStatusRequest request) {
        Integer collectorId = extractCollectorId(jwt);
        collectorService.updateStatus(requestId, collectorId, request.getStatus());
        return ApiResponse.<CollectionRequestActionResponse>builder()
                .result(CollectionRequestActionResponse.builder()
                        .collectionRequestId(requestId)
                        .status(request.getStatus().toLowerCase())
                        .actionAt(LocalDateTime.now())
                        .build())
                .build();
    }

    /**
     * Collector xác nhận hoàn tất thu gom (bắt buộc có ảnh):
     * - Tạo collector_report (kèm ảnh)
     * - Cập nhật collection_request.status: collected -> completed
     */
    @PostMapping(value = "/{requestId}/complete", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('COLLECTOR')")
    public ApiResponse<CollectorReportResponse> completeTask(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Integer requestId,
            @Valid @RequestPart("report") CreateCollectorReportRequest request,
            @RequestPart("images") @NotNull @Size(min = 1) List<MultipartFile> images) {
        Integer collectorId = extractCollectorId(jwt);
        request.setCollectionRequestId(requestId);
        CollectorReportResponse response = collectorReportService.createCollectorReport(request, images, collectorId);
        return ApiResponse.<CollectorReportResponse>builder()
                .result(response)
                .build();
    }

    /**
     * Lấy report theo collection request
     */
    @GetMapping("/{requestId}/report")
    @PreAuthorize("hasRole('COLLECTOR')")
    @Operation(summary = "Xem report theo yêu cầu", description = "Lấy collector_report gắn với collection request")
    public ApiResponse<CollectorReportResponse> getReportByCollectionRequest(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Integer requestId) {
        Integer collectorId = extractCollectorId(jwt);
        CollectorReportResponse response = collectorReportService.getReportByCollectionRequest(requestId, collectorId);

        if (response == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Report chưa được tạo cho collection request này");
        }

        return ApiResponse.<CollectorReportResponse>builder()
                .result(response)
                .build();
    }

    /**
     * Lấy danh sách report của collector hiện tại.
     */
    @GetMapping("/list_reports")
    @PreAuthorize("hasRole('COLLECTOR')")
    @Operation(summary = "Danh sách report của tôi", description = "Danh sách báo cáo đã gửi")
    public ApiResponse<java.util.List<CollectorReportResponse>> getMyReports(
            @AuthenticationPrincipal Jwt jwt) {
        Integer collectorId = extractCollectorId(jwt);
        java.util.List<CollectorReportResponse> reports = collectorReportService.getReportsByCollector(collectorId);

        return ApiResponse.<java.util.List<CollectorReportResponse>>builder()
                .result(reports)
                .build();
    }

    /**
     * Lấy chi tiết report theo reportId (chỉ cho report thuộc collector hiện tại).
     */
    @GetMapping("/reports/{reportId}")
    @PreAuthorize("hasRole('COLLECTOR')")
    @Operation(summary = "Chi tiết report", description = "Lấy chi tiết báo cáo theo reportId (thuộc collector hiện tại)")
    public ApiResponse<CollectorReportResponse> getReportById(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Integer reportId) {
        Integer collectorId = extractCollectorId(jwt);
        CollectorReportResponse response = collectorReportService.getReportById(reportId, collectorId);

        return ApiResponse.<CollectorReportResponse>builder()
                .result(response)
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
