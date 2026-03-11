package com.team2.Crowdsourced_Waste_Collection_Recycling_System.controller.admin;

import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.request.UpdateWasteCategoryRequest;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.request.WasteCategoryRequest;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response.ApiResponse;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response.WasteCategoryAdminResponse;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.service.WasteCategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/waste-categories")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
@Tag(name = "Admin – Waste Category Management", description = "Quản lý loại rác và cấu hình điểm thưởng")
public class AdminWasteCategoryController {

    private final WasteCategoryService wasteCategoryService;

    @GetMapping
    @Operation(summary = "Lấy danh sách tất cả loại rác")
    public ResponseEntity<ApiResponse<List<WasteCategoryAdminResponse>>> getAllCategories() {
        return ResponseEntity.ok(ApiResponse.<List<WasteCategoryAdminResponse>>builder()
                .result(wasteCategoryService.getAllCategories())
                .message("Lấy danh sách loại rác thành công")
                .build());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Lấy chi tiết một loại rác")
    public ResponseEntity<ApiResponse<WasteCategoryAdminResponse>> getCategory(@PathVariable Integer id) {
        return ResponseEntity.ok(ApiResponse.<WasteCategoryAdminResponse>builder()
                .result(wasteCategoryService.getCategory(id))
                .message("Lấy thông tin loại rác thành công")
                .build());
    }

    @PostMapping
    @Operation(summary = "Tạo loại rác mới", description = "Tạo loại rác mới với cấu hình điểm/đơn vị")
    public ResponseEntity<ApiResponse<WasteCategoryAdminResponse>> createCategory(
            @Valid @RequestBody WasteCategoryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<WasteCategoryAdminResponse>builder()
                        .result(wasteCategoryService.createCategory(request))
                        .message("Tạo loại rác thành công")
                        .build());
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Cập nhật loại rác", description = "Chỉ cập nhật các trường được gửi lên (bỏ trống hoặc null sẽ giữ nguyên giá trị cũ)")
    public ResponseEntity<ApiResponse<WasteCategoryAdminResponse>> updateCategory(
            @PathVariable Integer id,
            @Valid @RequestBody UpdateWasteCategoryRequest request) {
        return ResponseEntity.ok(ApiResponse.<WasteCategoryAdminResponse>builder()
                .result(wasteCategoryService.updateCategory(id, request))
                .message("Cập nhật loại rác thành công")
                .build());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Xóa loại rác")
    public ResponseEntity<ApiResponse<Void>> deleteCategory(@PathVariable Integer id) {
        wasteCategoryService.deleteCategory(id);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .message("Xóa loại rác thành công")
                .build());
    }
}
