package com.team2.Crowdsourced_Waste_Collection_Recycling_System.controller.admin;

import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response.ApiResponse;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response.CitizenLeaderboardResponse;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.service.WasteReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/points")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
@Tag(name = "Admin – Point Management", description = "Xem điểm của toàn bộ citizen")
public class AdminPointController {

    private final WasteReportService wasteReportService;

    @GetMapping("/citizens")
    @Operation(summary = "Lấy danh sách điểm của tất cả citizen")
    public ResponseEntity<ApiResponse<List<CitizenLeaderboardResponse>>> getAllCitizenPoints() {
        List<CitizenLeaderboardResponse> result = wasteReportService.getLeaderboard(null);
        return ResponseEntity.ok(ApiResponse.<List<CitizenLeaderboardResponse>>builder()
                .result(result)
                .message("Lấy danh sách điểm citizen thành công")
                .build());
    }
}
