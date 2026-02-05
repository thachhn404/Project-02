package com.team2.Crowdsourced_Waste_Collection_Recycling_System.controller.collector;

import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.request.CreateCollectorReportRequest;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.request.RejectTaskRequest;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response.ApiResponse;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response.CollectionRequestActionResponse;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response.CollectorReportResponse;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.collector.CollectionRequestRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.service.CollectorReportService;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.service.CollectorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

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
    private final CollectorReportService collectorReportService;

    /**
     * Lấy danh sách task của collector.
     * - Mặc định: chỉ hiển thị task active (assigned/accepted_collector/on_the_way)
     * - status: lọc theo trạng thái cụ thể
     * - all=true: lấy toàn bộ task
     */
    @GetMapping("/tasks")
    @PreAuthorize("hasRole('COLLECTOR')")
    public ApiResponse<List<CollectionRequestRepository.CollectorTaskView>> getTasks(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "all", required = false, defaultValue = "false") boolean all) {
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

    /**
     * Collector nhận task: assigned -> accepted_collector.
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
     * Collector hoàn tất task:
     * - Tạo collector_report (kèm ảnh)
     * - Cập nhật collection_request.status -> collected
     */
    @PostMapping(value = "/{requestId}/complete", consumes = {"multipart/form-data"})
    @PreAuthorize("hasRole('COLLECTOR')")
    public ApiResponse<CollectorReportResponse> completeTask(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Integer requestId,
            @Valid @ModelAttribute CreateCollectorReportRequest request) {
        Integer collectorId = extractCollectorId(jwt);
        request.setCollectionRequestId(requestId);
        CollectorReportResponse response = collectorReportService.createCollectorReport(request, collectorId);
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
    public ApiResponse<List<CollectorReportResponse>> getMyReports(
            @AuthenticationPrincipal Jwt jwt) {
        Integer collectorId = extractCollectorId(jwt);
        List<CollectorReportResponse> reports = collectorReportService.getReportsByCollector(collectorId);

        return ApiResponse.<List<CollectorReportResponse>>builder()
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
