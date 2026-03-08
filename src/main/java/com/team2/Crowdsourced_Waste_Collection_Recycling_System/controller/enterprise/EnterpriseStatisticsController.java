package com.team2.Crowdsourced_Waste_Collection_Recycling_System.controller.enterprise;

import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response.ApiResponse;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response.EnterpriseCitizenPointSummaryResponse;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response.EnterpriseWasteVolumeStatsResponse;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.service.EnterpriseStatisticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response.EnterpriseGeneralStatsResponse;

@RestController
@RequestMapping("/api/enterprise/reports")
@RequiredArgsConstructor
@Tag(name = "Enterprise Reports", description = "Báo cáo thống kê cho doanh nghiệp")
public class EnterpriseStatisticsController extends EnterpriseControllerSupport {

    private final EnterpriseStatisticsService enterpriseStatisticsService;

    @GetMapping("/general")
    @PreAuthorize("hasAnyRole('ENTERPRISE', 'ENTERPRISE_ADMIN')")
    @Operation(summary = "Thống kê tổng quan", description = "Tổng hợp báo cáo, khối lượng rác, hiệu suất collector, điểm thưởng")
    public ApiResponse<EnterpriseGeneralStatsResponse> getGeneralStats(@AuthenticationPrincipal Jwt jwt) {
        Integer enterpriseId = extractEnterpriseId(jwt);
        return ok(enterpriseStatisticsService.getGeneralStats(enterpriseId));
    }

    @GetMapping("/waste-volume")
    @PreAuthorize("hasAnyRole('ENTERPRISE', 'ENTERPRISE_ADMIN')")
    @Operation(summary = "Thống kê khối lượng rác thu được theo tháng & quý", description = "Tổng hợp theo năm dựa trên các request COMPLETED")
    public ApiResponse<EnterpriseWasteVolumeStatsResponse> getWasteVolumeStats(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam Integer year) {
        Integer enterpriseId = extractEnterpriseId(jwt);
        return ok(enterpriseStatisticsService.getWasteVolumeStats(enterpriseId, year));
    }

    @GetMapping("/citizens")
    @PreAuthorize("hasAnyRole('ENTERPRISE', 'ENTERPRISE_ADMIN')")
    @Operation(summary = "Danh sách citizen và điểm theo kỳ", description = "Mặc định theo năm; có thể lọc theo quarter hoặc month")
    public ApiResponse<List<EnterpriseCitizenPointSummaryResponse>> getCitizenPointSummaries(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam Integer year,
            @RequestParam(required = false) Integer quarter,
            @RequestParam(required = false) Integer month) {
        Integer enterpriseId = extractEnterpriseId(jwt);
        return ok(enterpriseStatisticsService.getCitizenPointSummaries(enterpriseId, year, quarter, month));
    }
}

