package com.team2.Crowdsourced_Waste_Collection_Recycling_System.controller.enterprise;

import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.request.CreateVoucherRequest;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.request.UpdateVoucherRequest;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response.ApiResponse;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response.EnterpriseVoucherResponse;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.service.reward.EnterpriseVoucherService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import java.util.List;


@RestController
@RequestMapping("/api/enterprise/vouchers")
@RequiredArgsConstructor
@Tag(name = "Enterprise Voucher", description = "Quản trị voucher (thêm/sửa/xóa) cho doanh nghiệp")
public class EnterpriseVoucherController extends EnterpriseControllerSupport {
    private final EnterpriseVoucherService enterpriseVoucherService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ENTERPRISE', 'ENTERPRISE_ADMIN')")
    @Transactional(readOnly = true)
    @Operation(summary = "Danh sách voucher", description = "Liệt kê voucher (hỗ trợ lọc theo active)")
    public ApiResponse<List<EnterpriseVoucherResponse>> list(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(value = "active", required = false) Boolean active) {
        extractEnterpriseId(jwt, "Chỉ ENTERPRISE mới được quản trị voucher");
        return ok(enterpriseVoucherService.list(active));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ENTERPRISE', 'ENTERPRISE_ADMIN')")
    @Transactional(readOnly = true)
    @Operation(summary = "Chi tiết voucher", description = "Lấy thông tin chi tiết voucher theo id")
    public ApiResponse<EnterpriseVoucherResponse> getById(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Integer id) {
        extractEnterpriseId(jwt, "Chỉ ENTERPRISE mới được quản trị voucher");
        return ok(enterpriseVoucherService.getById(id));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ENTERPRISE', 'ENTERPRISE_ADMIN')")
    @Transactional
    @Operation(summary = "Tạo voucher", description = "Tạo mới voucher")
    public ApiResponse<EnterpriseVoucherResponse> create(
            @AuthenticationPrincipal Jwt jwt,
            @ModelAttribute CreateVoucherRequest request) {
        extractEnterpriseId(jwt, "Chỉ ENTERPRISE mới được quản trị voucher");
        EnterpriseVoucherResponse created = enterpriseVoucherService.create(request);
        return ok(created, "Tạo voucher thành công");
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ENTERPRISE', 'ENTERPRISE_ADMIN')")
    @Transactional
    @Operation(summary = "Cập nhật voucher", description = "Sửa voucher (cập nhật từng trường nếu được cung cấp)")
    public ApiResponse<EnterpriseVoucherResponse> update(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Integer id,
            @RequestBody UpdateVoucherRequest request) {
        extractEnterpriseId(jwt, "Chỉ ENTERPRISE mới được quản trị voucher");
        EnterpriseVoucherResponse updated = enterpriseVoucherService.update(id, request);
        return ok(updated, "Cập nhật voucher thành công");
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ENTERPRISE', 'ENTERPRISE_ADMIN')")
    @Transactional
    @Operation(summary = "Xóa voucher", description = "Xóa mềm voucher bằng cách tắt active")
    public ApiResponse<Void> delete(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Integer id) {
        extractEnterpriseId(jwt, "Chỉ ENTERPRISE mới được quản trị voucher");
        enterpriseVoucherService.softDelete(id);
        return ok("Xóa voucher thành công");
    }
}
