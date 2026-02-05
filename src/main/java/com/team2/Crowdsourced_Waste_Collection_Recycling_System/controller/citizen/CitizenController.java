package com.team2.Crowdsourced_Waste_Collection_Recycling_System.controller.citizen;

import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.request.CreateWasteReportRequest;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response.ApiResponse;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response.WasteReportResponse;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.service.WasteReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/citizen")
@RequiredArgsConstructor
public class CitizenController {

    private final WasteReportService wasteReportService;

    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('CITIZEN')")
    public ResponseEntity<String> getDashboard() {
        return ResponseEntity.ok("Hello Citizen! This is your dashboard.");
    }

    @PostMapping(value = "/reports", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('CITIZEN')")
    public ResponseEntity<ApiResponse<WasteReportResponse>> createReport(
            @ModelAttribute CreateWasteReportRequest request) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentPrincipalName = authentication.getName();

        WasteReportResponse response = wasteReportService.createReport(request, currentPrincipalName);

        ApiResponse<WasteReportResponse> apiResponse = ApiResponse.<WasteReportResponse>builder()
                .result(response)
                .message("Report created successfully")
                .build();

        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/reports")
    @PreAuthorize("hasRole('CITIZEN')")
    public ResponseEntity<ApiResponse<List<WasteReportResponse>>> getMyReports() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentPrincipalName = authentication.getName();

        List<WasteReportResponse> reports = wasteReportService.getMyReports(currentPrincipalName);

        ApiResponse<List<WasteReportResponse>> apiResponse = ApiResponse.<List<WasteReportResponse>>builder()
                .result(reports)
                .message("Reports retrieved successfully")
                .build();

        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/reports/{id}")
    @PreAuthorize("hasRole('CITIZEN')")
    public ResponseEntity<ApiResponse<WasteReportResponse>> getMyReportById(@PathVariable("id") Integer id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentPrincipalName = authentication.getName();

        WasteReportResponse report = wasteReportService.getMyReportById(id, currentPrincipalName);

        ApiResponse<WasteReportResponse> apiResponse = ApiResponse.<WasteReportResponse>builder()
                .result(report)
                .message("Report retrieved successfully")
                .build();

        return ResponseEntity.ok(apiResponse);
    }
}
