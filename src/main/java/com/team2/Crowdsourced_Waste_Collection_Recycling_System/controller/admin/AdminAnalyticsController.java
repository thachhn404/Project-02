package com.team2.Crowdsourced_Waste_Collection_Recycling_System.controller.admin;

import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response.AdminCollectedWeightChartResponse;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response.AdminCollectedWeightDailyChartResponse;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response.ApiResponse;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.service.AdminAnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/analytics")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
@Tag(name = "Admin – Analytics", description = "Phân tích dữ liệu toàn hệ thống")
public class AdminAnalyticsController {

    private final AdminAnalyticsService adminAnalyticsService;

    @GetMapping("/collected-weight")
    @Operation(summary = "Biểu đồ khối lượng rác đã thu gom toàn hệ thống")
    public ResponseEntity<ApiResponse<AdminCollectedWeightChartResponse>> getCollectedWeightChart(
            @RequestParam(name = "year", required = false) Integer year) {
        AdminCollectedWeightChartResponse result = adminAnalyticsService.getCollectedWeightChart(year);
        return ResponseEntity.ok(ApiResponse.<AdminCollectedWeightChartResponse>builder()
                .result(result)
                .message("Lấy dữ liệu biểu đồ khối lượng rác thành công")
                .build());
    }

    @GetMapping("/collected-weight/daily")
    @Operation(summary = "Biểu đồ khối lượng rác theo ngày")
    public ResponseEntity<ApiResponse<AdminCollectedWeightDailyChartResponse>> getCollectedWeightDailyChart(
            @RequestParam(name = "year", required = false) Integer year,
            @RequestParam(name = "month", required = false) Integer month) {
        AdminCollectedWeightDailyChartResponse result = adminAnalyticsService.getCollectedWeightDailyChart(year, month);
        return ResponseEntity.ok(ApiResponse.<AdminCollectedWeightDailyChartResponse>builder()
                .result(result)
                .message("Lấy dữ liệu biểu đồ khối lượng rác theo ngày thành công")
                .build());
    }
}
