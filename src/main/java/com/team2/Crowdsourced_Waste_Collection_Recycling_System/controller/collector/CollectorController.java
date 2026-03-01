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
