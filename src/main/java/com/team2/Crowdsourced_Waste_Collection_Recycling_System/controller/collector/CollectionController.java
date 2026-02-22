package com.team2.Crowdsourced_Waste_Collection_Recycling_System.controller.collector;

import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.request.CreateCollectorReportRequest;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.request.RejectTaskRequest;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.request.UpdateTaskStatusRequest;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response.*;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.service.CollectorReportService;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.service.CollectorService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
    public ApiResponse<Page<CollectorTaskResponse>> getTasks(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "all", required = false, defaultValue = "false") boolean all,
            @RequestParam(value = "page", required = false, defaultValue = "1") @Min(1) Integer page,
            @RequestParam(value = "size", required = false, defaultValue = "10") @Min(1) Integer size) {
        Integer collectorId = extractCollectorId(jwt);
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<CollectorTaskResponse> tasks = collectorService.getTasks(collectorId, status, all, pageable);
        return ApiResponse.<Page<CollectorTaskResponse>>builder().result(tasks).build();
    }

    @GetMapping("/work_history")
    @PreAuthorize("hasRole('COLLECTOR')")
    public ApiResponse<Page<CollectorWorkHistoryItemResponse>> getWorkHistory(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "page", required = false, defaultValue = "1") @Min(1) Integer page,
            @RequestParam(value = "size", required = false, defaultValue = "10") @Min(1) Integer size) {
        Integer collectorId = extractCollectorId(jwt);
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<CollectorWorkHistoryItemResponse> result = collectorService.getWorkHistory(collectorId, status, pageable);
        return ApiResponse.<Page<CollectorWorkHistoryItemResponse>>builder()
                .result(result)
                .build();
    }

    @GetMapping("/stats")
    @PreAuthorize("hasRole('COLLECTOR')")
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
    public ApiResponse<Page<CollectorReportResponse>> getMyReports(
            @AuthenticationPrincipal Jwt jwt,
            // pagination: page bat dau tu 1
            @RequestParam(value = "page", required = false, defaultValue = "1") @Min(1) Integer page,
            // pagination: size toi da 12
            @RequestParam(value = "size", required = false, defaultValue = "12") @Min(1) @Max(12) Integer size) {
        Integer collectorId = extractCollectorId(jwt);
        Page<CollectorReportResponse> reports = collectorReportService.getReportsByCollector(
                collectorId,
                // page index bat dau tu 0
                PageRequest.of(page - 1, size));

        return ApiResponse.<Page<CollectorReportResponse>>builder()
                .result(reports)
                .build();
    }

    /**
     * Lấy chi tiết report theo reportId (chỉ cho report thuộc collector hiện tại).
     */
    @GetMapping("/reports/{reportId}")
    @PreAuthorize("hasRole('COLLECTOR')")
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
