package com.team2.Crowdsourced_Waste_Collection_Recycling_System.controller.enterprise;

import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.request.AssignCollectorRequest;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.request.AcceptWasteReportRequest;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.request.RejectWasteReportRequest;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response.ApiResponse;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response.AssignCollectorResponse;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response.EnterpriseRequestReportDetailResponse;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response.EligibleCollectorResponse;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response.CollectionRequestActionResponse;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.service.EnterpriseAssignmentService;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.service.EnterpriseReportDetailService;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.service.EnterpriseRequestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
 * Controller dành cho Doanh nghiệp tái chế (Enterprise).
 */
@RestController
@RequestMapping("/api/enterprise/requests")
@RequiredArgsConstructor
@Tag(name = "Enterprise Requests", description = "Luồng gán và điều phối Collector")
public class EnterpriseController {

    private final EnterpriseAssignmentService enterpriseAssignmentService;
    private final EnterpriseRequestService enterpriseRequestService;
    private final EnterpriseReportDetailService enterpriseReportDetailService;

    @PostMapping("/reports/{reportCode}/assign-collector")
    @PreAuthorize("hasRole('ENTERPRISE')")
    @Operation(summary = "Gán Collector theo reportCode (1 bước)", description = "Nếu chưa có CollectionRequest thì tự tạo (ACCEPTED_ENTERPRISE), sau đó gán thành ASSIGNED")
    public ApiResponse<AssignCollectorResponse> assignCollectorByReportCode(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable String reportCode,
            @RequestBody AssignCollectorRequest request) {
        Integer enterpriseId = extractEnterpriseId(jwt);
        AssignCollectorResponse result = enterpriseAssignmentService.assignCollectorByReportCode(enterpriseId, reportCode,
                request.getCollectorId());
        return ApiResponse.<AssignCollectorResponse>builder().result(result).build();
    }

    @GetMapping("/{requestId}/eligible-collectors")
    @PreAuthorize("hasRole('ENTERPRISE')")
    @Operation(summary = "Liệt kê Collector đủ điều kiện", description = "Lọc theo bán kính ≤10km (mặc định), online và trạng thái ACTIVE/AVAILABLE")
    public ApiResponse<List<EligibleCollectorResponse>> getEligibleCollectors(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Integer requestId,
            @RequestParam(name = "radiusKm", required = false) Double radiusKm) {
        Integer enterpriseId = extractEnterpriseId(jwt);
        List<EligibleCollectorResponse> result = enterpriseAssignmentService.findEligibleCollectors(enterpriseId, requestId, radiusKm);
        return ApiResponse.<List<EligibleCollectorResponse>>builder().result(result).build();
    }

    @GetMapping("/{requestId}/report-detail")
    @PreAuthorize("hasAnyRole('ENTERPRISE', 'ENTERPRISE_ADMIN')")
    @Operation(summary = "Chi tiết báo cáo theo request", description = "Gồm waste report và collector report (nếu đã tạo)")
    public ApiResponse<EnterpriseRequestReportDetailResponse> getRequestReportDetail(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Integer requestId) {
        Integer enterpriseId = extractEnterpriseId(jwt);
        EnterpriseRequestReportDetailResponse result = enterpriseReportDetailService.getRequestReportDetail(enterpriseId, requestId);
        return ApiResponse.<EnterpriseRequestReportDetailResponse>builder().result(result).build();
    }

    /**
     * Enterprise accept một WasteReport và tự động tạo CollectionRequest
     */
    @PostMapping("/accept/{reportCode}")
    @PreAuthorize("hasRole('ENTERPRISE')")
    @Operation(summary = "Accept WasteReport và tạo CollectionRequest", description = "Enterprise accept báo cáo rác và tạo mới yêu cầu thu gom ở trạng thái PENDING")
    public ApiResponse<CollectionRequestActionResponse> acceptWasteReport(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable String reportCode,
            @RequestBody(required = false) AcceptWasteReportRequest body) {
        Integer enterpriseId = extractEnterpriseId(jwt);
        Integer collectionRequestId = enterpriseRequestService.acceptWasteReport(
                enterpriseId, reportCode);

        return ApiResponse.<CollectionRequestActionResponse>builder()
                .result(CollectionRequestActionResponse.builder()
                        .collectionRequestId(collectionRequestId)
                        .status("accepted")
                        .actionAt(LocalDateTime.now())
                        .build())
                .build();
    }

    @PostMapping("/reject/{reportCode}")
    @PreAuthorize("hasRole('ENTERPRISE')")
    @Operation(summary = "Reject WasteReport", description = "Enterprise từ chối báo cáo rác, cập nhật trạng thái REJECTED")
    public ApiResponse<Void> rejectWasteReport(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable String reportCode,
            @RequestBody(required = false) RejectWasteReportRequest body) {
        Integer enterpriseId = extractEnterpriseId(jwt);
        String reason = body != null ? body.getReason() : null;
        enterpriseRequestService.rejectWasteReport(enterpriseId, reportCode, reason);
        return ApiResponse.<Void>builder().message("Rejected").build();
    }

    private Integer extractEnterpriseId(Jwt jwt) {
        if (jwt == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Thiếu token");
        }
        Object value = jwt.getClaims().get("enterpriseId");
        if (value == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User hiện tại không phải Enterprise");
        }
        if (value instanceof Number number) {
            return number.intValue();
        }
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "enterpriseId không hợp lệ");
    }
}
