package com.team2.Crowdsourced_Waste_Collection_Recycling_System.controller.enterprise;

import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response.ApiResponse;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response.EnterpriseWasteReportResponse;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.service.EnterpriseWasteReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/enterprise/waste-reports")
@RequiredArgsConstructor
public class EnterpriseWasteReportController extends EnterpriseControllerSupport {

    private final EnterpriseWasteReportService enterpriseWasteReportService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ENTERPRISE', 'ENTERPRISE_ADMIN')")
    public ResponseEntity<ApiResponse<List<EnterpriseWasteReportResponse>>> getReports(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(name = "status", required = false) String status) {

        Integer enterpriseId = extractEnterpriseId(jwt);
        List<EnterpriseWasteReportResponse> result = enterpriseWasteReportService.getReports(enterpriseId, status);

        return okEntity(result, "Lấy danh sách báo cáo thành công");
    }

    @GetMapping("/pending")
    @PreAuthorize("hasAnyRole('ENTERPRISE', 'ENTERPRISE_ADMIN')")
    public ResponseEntity<ApiResponse<List<EnterpriseWasteReportResponse>>> getPendingReports(
            @AuthenticationPrincipal Jwt jwt) {

        Integer enterpriseId = extractEnterpriseId(jwt);
        List<EnterpriseWasteReportResponse> result = enterpriseWasteReportService.getPendingReports(enterpriseId);

        return okEntity(result, "Lấy danh sách báo cáo PENDING phù hợp thành công");
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ENTERPRISE', 'ENTERPRISE_ADMIN')")
    public ResponseEntity<ApiResponse<EnterpriseWasteReportResponse>> getReportById(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable("id") Integer id) {
        Integer enterpriseId = extractEnterpriseId(jwt);
        EnterpriseWasteReportResponse result = enterpriseWasteReportService.getReportById(enterpriseId, id);

        return okEntity(result, "Lấy chi tiết báo cáo thành công");
    }
}
