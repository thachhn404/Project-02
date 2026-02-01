package com.team2.Crowdsourced_Waste_Collection_Recycling_System.controller;

import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.request.AssignCollectorRequest;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response.ApiResponse;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response.AssignCollectorResponse;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.CollectionRequest;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.CollectionRequestRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.service.EnterpriseAssignmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * Controller dành cho Doanh nghiệp tái chế (Enterprise).
 */
@RestController
@RequestMapping("/api/enterprise/requests")
@RequiredArgsConstructor
public class EnterpriseController {

    private final CollectionRequestRepository collectionRequestRepository;
    private final EnterpriseAssignmentService enterpriseAssignmentService;

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
    @PreAuthorize("hasRole('ENTERPRISE')")
    public ApiResponse<AssignCollectorResponse> assignCollector(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Integer id,
            @RequestBody(required = false) AssignCollectorRequest request,
            @RequestParam(value = "collectorId", required = false) Integer collectorIdParam
    ) {
        Integer enterpriseId = extractEnterpriseId(jwt);
        Integer collectorId = collectorIdParam != null ? collectorIdParam : (request != null ? request.getCollectorId() : null);
        AssignCollectorResponse result = enterpriseAssignmentService.assignCollector(enterpriseId, id, collectorId);
        return ApiResponse.<AssignCollectorResponse>builder().result(result).build();
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
