package com.team2.Crowdsourced_Waste_Collection_Recycling_System.controller.admin;

import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.request.AdminCreateUserRequest;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response.AdminUserResponse;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response.ApiResponse;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.service.AdminAccountService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * Admin – Quản lý tài khoản trong hệ thống.
 * Tất cả các endpoint yêu cầu role ADMIN.
 *
 * Base URL: /api/admin/accounts
 */
@RestController
@RequestMapping("/api/admin/accounts")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
@Tag(name = "Admin – Account Management", description = "Quản lý toàn bộ tài khoản người dùng trong hệ thống")
public class AdminAccountController {

    private final AdminAccountService adminAccountService;

    /**
     * Admin tạo tài khoản cho role CITIZEN/COLLECTOR/ENTERPRISE.
     * Không cho phép tạo ADMIN hoặc ENTERPRISE_ADMIN.
     */
    @PostMapping
    @Operation(summary = "Tạo tài khoản", description = "Admin tạo user cho role CITIZEN, COLLECTOR, ENTERPRISE")
    public ApiResponse<AdminUserResponse> createUser(
            @RequestBody AdminCreateUserRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        String adminEmail = extractAdminEmail(jwt);
        return ApiResponse.<AdminUserResponse>builder()
                .result(adminAccountService.createUser(request, adminEmail))
                .build();
    }

    /**
     * Lấy danh sách toàn bộ tài khoản.
     * Có thể lọc theo status (active/suspended) và/hoặc role (CITIZEN,
     * COLLECTOR...).
     */
    @GetMapping
    @Operation(summary = "Lấy danh sách tài khoản", description = "Lọc tuỳ chọn theo status và roleCode")
    public ApiResponse<List<AdminUserResponse>> getAllUsers(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String role,
            @AuthenticationPrincipal Jwt jwt) {
        String adminEmail = extractAdminEmail(jwt);
        return ApiResponse.<List<AdminUserResponse>>builder()
                .result(adminAccountService.getAllUsers(status, role, adminEmail))
                .build();
    }

    /**
     * Xem chi tiết một tài khoản.
     */
    @GetMapping("/{userId}")
    @Operation(summary = "Xem chi tiết tài khoản")
    public ApiResponse<AdminUserResponse> getUserDetail(@PathVariable Integer userId) {
        return ApiResponse.<AdminUserResponse>builder()
                .result(adminAccountService.getUserDetail(userId))
                .build();
    }

    /**
     * Khóa tài khoản (status → suspended).
     * Admin không thể tự khóa chính mình.
     */
    @PatchMapping("/{userId}/suspend")
    @Operation(summary = "Khóa tài khoản")
    public ApiResponse<AdminUserResponse> suspendUser(
            @PathVariable Integer userId,
            @AuthenticationPrincipal Jwt jwt) {
        String adminEmail = extractAdminEmail(jwt);
        return ApiResponse.<AdminUserResponse>builder()
                .result(adminAccountService.suspendUser(userId, adminEmail))
                .build();
    }

    /**
     * Mở khóa tài khoản (status → active).
     */
    @PatchMapping("/{userId}/activate")
    @Operation(summary = "Mở khóa tài khoản")
    public ApiResponse<AdminUserResponse> activateUser(
            @PathVariable Integer userId,
            @AuthenticationPrincipal Jwt jwt) {
        String adminEmail = extractAdminEmail(jwt);
        return ApiResponse.<AdminUserResponse>builder()
                .result(adminAccountService.activateUser(userId, adminEmail))
                .build();
    }

    // ─────────────────────────────────────────────
    // Private helpers
    // ─────────────────────────────────────────────

    /**
     * Lấy email của admin từ JWT subject (claim "sub").
     */
    private String extractAdminEmail(Jwt jwt) {
        if (jwt == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Thiếu token");
        }
        String subject = jwt.getSubject(); // email của admin hiện tại
        if (subject == null || subject.isBlank()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Không tìm thấy thông tin admin trong token");
        }
        return subject;
    }
}
