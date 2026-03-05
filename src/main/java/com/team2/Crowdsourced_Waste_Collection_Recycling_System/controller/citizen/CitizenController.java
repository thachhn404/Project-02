package com.team2.Crowdsourced_Waste_Collection_Recycling_System.controller.citizen;

import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.request.CreateWasteReportRequest;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.request.UpdateWasteReportRequest;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response.ApiResponse;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response.CitizenLeaderboardResponse;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response.CitizenReportResultResponse;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.request.CreateComplaintRequest;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response.ComplaintResponse;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response.CitizenRewardHistoryResponse;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response.WasteCategoryResponse;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response.WasteReportResponse;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.service.WasteReportService;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/citizen")
@RequiredArgsConstructor
@Tag(name = "Citizen", description = "Tác vụ của công dân: báo cáo rác, khiếu nại, thưởng")
public class CitizenController {
    private final WasteReportService wasteReportService;

    @PostMapping(value = "/reports", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('CITIZEN')")
    @Operation(summary = "Tạo báo cáo rác", description = "Gửi báo cáo kèm ảnh, vị trí và mô tả")
    public ResponseEntity<ApiResponse<WasteReportResponse>> createReport(@Valid @ModelAttribute CreateWasteReportRequest request) {
        WasteReportResponse response = wasteReportService.createReport(request, currentEmail());
        return ok(response, "Tạo báo cáo thành công");
    }

    @PutMapping(value = "/reports/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('CITIZEN')")
    @Operation(summary = "Cập nhật báo cáo rác", description = "Chỉnh sửa nội dung, ảnh của báo cáo đã tạo")
    public ResponseEntity<ApiResponse<WasteReportResponse>> updateReport(
            @PathVariable("id") Integer id,
            @Valid @ModelAttribute UpdateWasteReportRequest request) {
        WasteReportResponse response = wasteReportService.updateReport(id, request, currentEmail());
        return ok(response, "Cập nhật báo cáo thành công");
    }

    @DeleteMapping("/reports/{id}")
    @PreAuthorize("hasRole('CITIZEN')")
    @Operation(summary = "Huỷ báo cáo rác", description = "Xoá báo cáo của tôi theo ID")
    public ResponseEntity<ApiResponse<Void>> deleteReport(@PathVariable("id") Integer id) {
        wasteReportService.deleteReport(id, currentEmail());
        return ok("Huỷ báo cáo thành công");
    }

    @GetMapping("/reports")
    @PreAuthorize("hasRole('CITIZEN')")
    @Operation(summary = "Danh sách báo cáo của tôi", description = "Trả về các báo cáo rác do tôi tạo")
    public ResponseEntity<ApiResponse<List<WasteReportResponse>>> getMyReports() {
        List<WasteReportResponse> reports = wasteReportService.getMyReports(currentEmail());
        return ok(reports, "Lấy danh sách báo cáo thành công");
    }

    @GetMapping("/reports/{id}")
    @PreAuthorize("hasAnyRole('CITIZEN','ENTERPRISE')")
    @Operation(summary = "Chi tiết báo cáo của tôi", description = "Lấy chi tiết báo cáo theo ID")
    public ResponseEntity<ApiResponse<WasteReportResponse>> getMyReportById(@PathVariable("id") Integer id) {
        WasteReportResponse report = wasteReportService.getMyReportById(id, currentEmail());
        return ok(report, "Lấy chi tiết báo cáo thành công");
    }

    @GetMapping("/reports/{id}/result")
    @PreAuthorize("hasRole('CITIZEN')")
    @Operation(summary = "Kết quả xử lý báo cáo", description = "Xem kết quả thu gom của báo cáo")
    public ResponseEntity<ApiResponse<CitizenReportResultResponse>> getMyReportResult(@PathVariable("id") Integer id) {
        CitizenReportResultResponse result = wasteReportService.getMyReportResult(id, currentEmail());
        return ok(result, "Lấy kết quả thu gom thành công");
    }

    @GetMapping("/rewards/history")
    @PreAuthorize("hasRole('CITIZEN')")
    @Operation(summary = "Lịch sử điểm thưởng", description = "Lọc lịch sử theo khoảng thời gian tùy chọn")
    public ResponseEntity<ApiResponse<List<CitizenRewardHistoryResponse>>> getRewardHistory(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        List<CitizenRewardHistoryResponse> history = wasteReportService.getRewardHistory(currentEmail(), startDate, endDate);
        return ok(history, "Lấy lịch sử điểm thưởng thành công");
    }

    @GetMapping("/leaderboard")
    @Operation(summary = "Bảng xếp hạng công dân", description = "Xếp hạng theo điểm, hỗ trợ lọc theo khu vực")
    public ResponseEntity<ApiResponse<List<CitizenLeaderboardResponse>>> getLeaderboard(
            @RequestParam(required = false) String region) {
        List<CitizenLeaderboardResponse> leaderboard = wasteReportService.getLeaderboard(region);
        return ResponseEntity.ok(ApiResponse.<List<CitizenLeaderboardResponse>>builder()
                .result(leaderboard)
                .message("Lấy bảng xếp hạng thành công")
                .build());
    }

    @PostMapping("/complaints")
    @PreAuthorize("hasRole('CITIZEN')")
    @Operation(summary = "Tạo khiếu nại", description = "Gửi khiếu nại liên quan đến thu gom")
    public ResponseEntity<ApiResponse<ComplaintResponse>> createComplaint(
            @Valid @RequestBody CreateComplaintRequest request) {
        ComplaintResponse response = wasteReportService.createComplaint(request, currentEmail());
        return ok(response, "Tạo khiếu nại thành công");
    }

    @GetMapping("/complaints")
    @PreAuthorize("hasRole('CITIZEN')")
    @Operation(summary = "Danh sách khiếu nại", description = "Liệt kê các khiếu nại của tôi")
    public ResponseEntity<ApiResponse<List<ComplaintResponse>>> getComplaints() {
        List<ComplaintResponse> complaints = wasteReportService.getComplaints(currentEmail());
        return ok(complaints, "Lấy danh sách khiếu nại thành công");
    }

    @GetMapping("/waste-categories")
    @PreAuthorize("hasRole('CITIZEN')")
    @Operation(summary = "Danh mục loại rác", description = "Danh sách các loại rác hỗ trợ")
    public ResponseEntity<ApiResponse<List<WasteCategoryResponse>>> getWasteCategories() {
        List<WasteCategoryResponse> categories = wasteReportService.getWasteCategories();
        return ok(categories, "Lấy danh sách loại rác thành công");
    }

    private String currentEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName();
    }

    private ResponseEntity<ApiResponse<Void>> ok(String message) {
        return ResponseEntity.ok(ApiResponse.<Void>builder().message(message).build());
    }

    private <T> ResponseEntity<ApiResponse<T>> ok(T result, String message) {
        return ResponseEntity.ok(ApiResponse.<T>builder().result(result).message(message).build());
    }
}
