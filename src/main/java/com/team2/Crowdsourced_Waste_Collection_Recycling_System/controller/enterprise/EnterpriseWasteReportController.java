package com.team2.Crowdsourced_Waste_Collection_Recycling_System.controller.enterprise;

import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response.ApiResponse;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response.EnterpriseWasteReportResponse;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.service.EnterpriseWasteReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/enterprise/waste-reports")
@RequiredArgsConstructor
public class EnterpriseWasteReportController {

    private final EnterpriseWasteReportService enterpriseWasteReportService;

    @GetMapping("/pending")
    @PreAuthorize("hasAnyRole('ENTERPRISE', 'ENTERPRISE_ADMIN')")
    public ResponseEntity<ApiResponse<List<EnterpriseWasteReportResponse>>> getPendingReports(
            @AuthenticationPrincipal Jwt jwt) {

        Integer enterpriseId = extractEnterpriseId(jwt);
        List<EnterpriseWasteReportResponse> result = enterpriseWasteReportService.getPendingReports(enterpriseId);

        return ResponseEntity.ok(ApiResponse.<List<EnterpriseWasteReportResponse>>builder()
                .result(result)
                .message("Lấy danh sách báo cáo PENDING phù hợp thành công")
                .build());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ENTERPRISE', 'ENTERPRISE_ADMIN')")
    public ResponseEntity<ApiResponse<EnterpriseWasteReportResponse>> getReportById(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable("id") Integer id) {
        Integer enterpriseId = extractEnterpriseId(jwt);
        EnterpriseWasteReportResponse result = enterpriseWasteReportService.getReportById(enterpriseId, id);

        return ResponseEntity.ok(ApiResponse.<EnterpriseWasteReportResponse>builder()
                .result(result)
                .message("Lấy chi tiết báo cáo thành công")
                .build());
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
