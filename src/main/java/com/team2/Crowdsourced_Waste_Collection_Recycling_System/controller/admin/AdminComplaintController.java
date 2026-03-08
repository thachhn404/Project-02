package com.team2.Crowdsourced_Waste_Collection_Recycling_System.controller.admin;

import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.request.EnterpriseFeedbackResolveRequest;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response.ApiResponse;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response.EnterpriseFeedbackResponse;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.service.AdminComplaintService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/complaints")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
@Tag(name = "Admin – Complaint Management", description = "Quản lý khiếu nại (admin supervision)")
public class AdminComplaintController {

    private final AdminComplaintService adminComplaintService;

    @GetMapping
    @Operation(summary = "Danh sách tất cả khiếu nại", description = "Admin xem toàn bộ khiếu nại")
    public ResponseEntity<ApiResponse<List<EnterpriseFeedbackResponse>>> getAllComplaints() {
        return ResponseEntity.ok(ApiResponse.<List<EnterpriseFeedbackResponse>>builder()
                .result(adminComplaintService.getAllComplaints())
                .message("Lấy danh sách khiếu nại thành công")
                .build());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Chi tiết khiếu nại", description = "Xem chi tiết khiếu nại")
    public ResponseEntity<ApiResponse<EnterpriseFeedbackResponse>> getComplaintDetail(@PathVariable Integer id) {
        return ResponseEntity.ok(ApiResponse.<EnterpriseFeedbackResponse>builder()
                .result(adminComplaintService.getComplaintDetail(id))
                .message("Lấy chi tiết khiếu nại thành công")
                .build());
    }

    @PostMapping("/{id}/resolve")
    @Operation(summary = "Giải quyết khiếu nại (Admin)", description = "Admin đưa ra quyết định cuối cùng")
    public ResponseEntity<ApiResponse<Void>> resolveComplaint(
            @PathVariable Integer id,
            @Valid @RequestBody EnterpriseFeedbackResolveRequest request) {
        adminComplaintService.resolveComplaint(id, request);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .message("Giải quyết khiếu nại thành công")
                .build());
    }
}
