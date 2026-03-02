package com.team2.Crowdsourced_Waste_Collection_Recycling_System.controller.admin;

import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response.ApiResponse;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response.CollectorReportResponse;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response.EnterpriseWasteReportResponse;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.service.EnterpriseCollectorReportService;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.service.EnterpriseWasteReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/reports")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
@Tag(name = "Admin – Report Management", description = "Quản lý báo cáo rác và báo cáo thu gom")
public class AdminReportController {

    private final EnterpriseWasteReportService enterpriseWasteReportService;
    private final EnterpriseCollectorReportService enterpriseCollectorReportService;

    // ---------------- Waste Reports (Citizens) ----------------

    @GetMapping("/waste-reports")
    @Operation(summary = "Lấy danh sách báo cáo rác (Waste Reports)", 
               description = "Xem tất cả hoặc lọc theo enterpriseId và status")
    public ResponseEntity<ApiResponse<List<EnterpriseWasteReportResponse>>> getWasteReports(
            @RequestParam(name = "enterpriseId", required = false) Integer enterpriseId,
            @RequestParam(name = "status", required = false) String status) {

        List<EnterpriseWasteReportResponse> result = enterpriseWasteReportService.getReports(enterpriseId, status);

        return ResponseEntity.ok(ApiResponse.<List<EnterpriseWasteReportResponse>>builder()
                .result(result)
                .message("Lấy danh sách báo cáo rác thành công")
                .build());
    }

    @GetMapping("/waste-reports/pending")
    @Operation(summary = "Lấy danh sách báo cáo rác đang chờ (PENDING)", 
               description = "Xem tất cả PENDING hoặc lọc theo enterpriseId")
    public ResponseEntity<ApiResponse<List<EnterpriseWasteReportResponse>>> getPendingWasteReports(
            @RequestParam(name = "enterpriseId", required = false) Integer enterpriseId) {

        List<EnterpriseWasteReportResponse> result = enterpriseWasteReportService.getPendingReports(enterpriseId);

        return ResponseEntity.ok(ApiResponse.<List<EnterpriseWasteReportResponse>>builder()
                .result(result)
                .message("Lấy danh sách báo cáo rác PENDING thành công")
                .build());
    }

    @GetMapping("/waste/{id}")
    @Operation(summary = "Xem chi tiết báo cáo rác")
    public ResponseEntity<ApiResponse<EnterpriseWasteReportResponse>> getWasteReportById(
            @PathVariable("id") Integer id,
            @RequestParam(name = "enterpriseId", required = false) Integer enterpriseId) {
            
        EnterpriseWasteReportResponse result = enterpriseWasteReportService.getReportById(enterpriseId, id);

        return ResponseEntity.ok(ApiResponse.<EnterpriseWasteReportResponse>builder()
                .result(result)
                .message("Lấy chi tiết báo cáo rác thành công")
                .build());
    }

    // ---------------- Collector Reports (Collectors) ----------------

    @GetMapping("/collector")
    @Operation(summary = "Lấy danh sách báo cáo thu gom (Collector Reports)", 
               description = "Xem tất cả hoặc lọc theo enterpriseId và status")
    public ResponseEntity<ApiResponse<List<CollectorReportResponse>>> getCollectorReports(
            @RequestParam(name = "enterpriseId", required = false) Integer enterpriseId
//            @RequestParam(name = "status", required = false) String status
    ) {

        List<CollectorReportResponse> result = enterpriseCollectorReportService.getCollectorReports(enterpriseId);

        return ResponseEntity.ok(ApiResponse.<List<CollectorReportResponse>>builder()
                .result(result)
                .message("Lấy danh sách báo cáo thu gom thành công")
                .build());
    }
}
