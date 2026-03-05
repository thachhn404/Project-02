package com.team2.Crowdsourced_Waste_Collection_Recycling_System.controller.citizen;

import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response.ApiResponse;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response.VoucherRedemptionResponse;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response.VoucherResponse;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.service.reward.VoucherService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/citizen/vouchers")
@RequiredArgsConstructor
@Tag(name = "Citizen Voucher", description = "Voucher: xem danh sách, đổi điểm lấy voucher")
public class CitizenVoucherController {
    private final VoucherService voucherService;

    @GetMapping
    @PreAuthorize("hasRole('CITIZEN')")
    @Operation(summary = "Danh sách voucher", description = "Trả về các voucher đang active để công dân đổi điểm")
    public ResponseEntity<ApiResponse<List<VoucherResponse>>> getAvailableVouchers() {
        List<VoucherResponse> vouchers = voucherService.getAvailableVouchers();
        return ok(vouchers, "Lấy danh sách voucher thành công");
    }

    @PostMapping("/{voucherId}/redeem")
    @PreAuthorize("hasRole('CITIZEN')")
    @Operation(summary = "Đổi điểm lấy voucher", description = "Trừ điểm của công dân và tạo voucher đã đổi")
    public ResponseEntity<ApiResponse<VoucherRedemptionResponse>> redeem(@PathVariable Integer voucherId) {
        VoucherRedemptionResponse redeemed = voucherService.redeem(voucherId, currentEmail());
        return ok(redeemed, "Đổi voucher thành công");
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('CITIZEN')")
    @Operation(summary = "Voucher của tôi", description = "Danh sách voucher đã đổi của công dân")
    public ResponseEntity<ApiResponse<List<VoucherRedemptionResponse>>> getMyVouchers() {
        List<VoucherRedemptionResponse> my = voucherService.getMyVouchers(currentEmail());
        return ok(my, "Lấy voucher của tôi thành công");
    }

    private String currentEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName();
    }

    private <T> ResponseEntity<ApiResponse<T>> ok(T result, String message) {
        return ResponseEntity.ok(ApiResponse.<T>builder().result(result).message(message).build());
    }
}
