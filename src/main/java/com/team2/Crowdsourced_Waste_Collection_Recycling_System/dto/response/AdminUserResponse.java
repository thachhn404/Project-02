package com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response;

import lombok.Builder;
import lombok.Data;

/**
 * Response trả về thông tin tài khoản người dùng dành cho Admin.
 * Không bao gồm passwordHash hay thông tin nhạy cảm khác.
 */
@Data
@Builder
public class AdminUserResponse {
    private Integer id;
    private String email;
    private String fullName;
    private String phone;
    private String roleCode;
    private String roleName;
    private String status; // "active" | "suspended"
    private java.time.LocalDateTime createdAt;
}
