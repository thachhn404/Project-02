package com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Request body dành riêng cho Admin khi tạo tài khoản mới trong hệ thống.
 * khác với RegisterRequest (chỉ dành cho tự đăng ký với role CITIZEN),
 * request này cho phép Admin chỉ định role bất kỳ.
 */
@Data
public class AdminCreateAccountRequest {

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không hợp lệ")
    private String email;

    @NotBlank(message = "Mật khẩu không được để trống")
    @Size(min = 8, message = "Mật khẩu phải có ít nhất 8 ký tự")
    private String password;

    @NotBlank(message = "Họ tên không được để trống")
    private String fullName;

    private String phone;

    /**
     * Role cần gán cho tài khoản mới.
     * Giá trị hợp lệ: CITIZEN | COLLECTOR | ENTERPRISE | ENTERPRISE_ADMIN | ADMIN
     */
    @NotBlank(message = "Role không được để trống")
    private String roleCode;

    /**
     * Chỉ áp dụng khi roleCode = "COLLECTOR".
     * Nếu để trống, mặc định thuộc Enterprise id = 1.
     */
    private Integer enterpriseId;
}
