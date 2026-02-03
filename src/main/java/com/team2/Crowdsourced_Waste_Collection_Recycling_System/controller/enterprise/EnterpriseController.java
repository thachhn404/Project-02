package com.team2.Crowdsourced_Waste_Collection_Recycling_System.controller.enterprise;

import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.request.AssignCollectorRequest;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response.ApiResponse;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response.AssignCollectorResponse;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response.CollectionRequestActionResponse;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.CollectionRequest;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.collector.CollectionRequestRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.service.EnterpriseAssignmentService;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.service.EnterpriseRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.time.LocalDateTime;

/**
 * Controller dành cho Doanh nghiệp tái chế (Enterprise).
 */
@RestController
@RequestMapping("/api/enterprise/requests")
@RequiredArgsConstructor
public class EnterpriseController {

    private final CollectionRequestRepository collectionRequestRepository;
    private final EnterpriseAssignmentService enterpriseAssignmentService;
    private final EnterpriseRequestService enterpriseRequestService;

    /**
     * Xem tất cả yêu cầu thu gom thuộc về doanh nghiệp này.
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ENTERPRISE', 'ENTERPRISE_ADMIN')")
    public ResponseEntity<List<CollectionRequest>> getAllRequests(@AuthenticationPrincipal Jwt jwt) {
        Integer enterpriseId = extractEnterpriseId(jwt);
        return ResponseEntity.ok(collectionRequestRepository.findByEnterprise_Id(enterpriseId));
    }

    /**
     * Giao một yêu cầu thu gom cho một nhân viên (Collector).
     */
    @PostMapping("/{requestCode}/assign")
    @PreAuthorize("hasRole('ENTERPRISE')")
    public ApiResponse<AssignCollectorResponse> assignCollector(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable String requestCode,
            @RequestBody AssignCollectorRequest request
    ) {
        Integer enterpriseId = extractEnterpriseId(jwt);
        AssignCollectorResponse result = enterpriseAssignmentService.assignCollector(enterpriseId, requestCode, request.getCollectorId());
        return ApiResponse.<AssignCollectorResponse>builder().result(result).build();
    }

    @PostMapping("/{id}/assign")
    @PreAuthorize("hasRole('ENTERPRISE')")
    public ApiResponse<AssignCollectorResponse> assignCollector(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Integer id,
            @RequestBody AssignCollectorRequest request
    ) {
        Integer enterpriseId = extractEnterpriseId(jwt);
        AssignCollectorResponse result = enterpriseAssignmentService.assignCollector(enterpriseId, id, request.getCollectorId());
        return ApiResponse.<AssignCollectorResponse>builder().result(result).build();
    }

    @PostMapping("/{requestCode}/accept")
    @PreAuthorize("hasRole('ENTERPRISE')")
    public ApiResponse<CollectionRequestActionResponse> acceptRequest(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable String requestCode
    ) {
        Integer enterpriseId = extractEnterpriseId(jwt);
        Integer requestId = enterpriseRequestService.acceptRequest(enterpriseId, requestCode);
        return ApiResponse.<CollectionRequestActionResponse>builder()
                .result(CollectionRequestActionResponse.builder()
                        .collectionRequestId(requestId)
                        .status("accepted_enterprise")
                        .actionAt(LocalDateTime.now())
                        .build())
                .build();
    }

    @PostMapping("/{id}/accept")
    @PreAuthorize("hasRole('ENTERPRISE')")
    public ApiResponse<CollectionRequestActionResponse> acceptRequest(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Integer id
    ) {
        Integer enterpriseId = extractEnterpriseId(jwt);
        enterpriseRequestService.acceptRequest(enterpriseId, id);
        return ApiResponse.<CollectionRequestActionResponse>builder()
                .result(CollectionRequestActionResponse.builder()
                        .collectionRequestId(id)
                        .status("accepted_enterprise")
                        .actionAt(LocalDateTime.now())
                        .build())
                .build();
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
