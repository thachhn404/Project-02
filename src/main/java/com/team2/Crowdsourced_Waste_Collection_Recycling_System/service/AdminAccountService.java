package com.team2.Crowdsourced_Waste_Collection_Recycling_System.service;

import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.request.AdminCreateUserRequest;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response.AdminUserResponse;

import java.util.List;

/**
 * Các nghiệp vụ Admin – Quản lý tài khoản trong hệ thống.
 */
public interface AdminAccountService {

    /**
     * Admin tạo tài khoản mới cho các role được phép.
     *
     * Cho phép: CITIZEN, COLLECTOR, ENTERPRISE.
     * Không cho phép: ADMIN, ENTERPRISE_ADMIN.
     */
    AdminUserResponse createUser(AdminCreateUserRequest request, String adminEmail);

    /**
     * Lấy toàn bộ danh sách tài khoản.
     *
     * @param status   lọc theo trạng thái ("active" | "suspended"), null = tất cả
     * @param roleCode lọc theo role (vd. "COLLECTOR"), null = tất cả
     * @param adminEmail email của admin đang đăng nhập để loại trừ khỏi danh sách
     */
    List<AdminUserResponse> getAllUsers(String status, String roleCode, String adminEmail);

    /**
     * Xem chi tiết một tài khoản.
     */
    AdminUserResponse getUserDetail(Integer userId);

    /**
     * Khóa tài khoản (status → "suspended").
     *
     * @param userId     ID tài khoản cần khóa
     * @param adminEmail email của admin đang thực hiện (từ JWT sub, để guard tự
     *                   khóa chính mình)
     */
    AdminUserResponse suspendUser(Integer userId, String adminEmail);

    /**
     * Mở khóa tài khoản (status → "active").
     *
     * @param userId     ID tài khoản cần mở
     * @param adminEmail email của admin đang thực hiện
     */
    AdminUserResponse activateUser(Integer userId, String adminEmail);
}
