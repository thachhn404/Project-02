package com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Request body dành riêng cho Admin khi đổi role của một tài khoản.
 */
@Data
public class AdminChangeRoleRequest {

    /**
     * Role mới muốn gán.
     * Giá trị hợp lệ: CITIZEN | COLLECTOR | ENTERPRISE | ENTERPRISE_ADMIN | ADMIN
     */
    @NotBlank(message = "Role không được để trống")
    private String roleCode;
}
