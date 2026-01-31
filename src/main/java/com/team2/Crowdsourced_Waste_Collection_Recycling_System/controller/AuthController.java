package com.team2.Crowdsourced_Waste_Collection_Recycling_System.controller;

import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.request.*;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response.*;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.service.AuthService;
import com.nimbusds.jose.JOSEException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
/**
 * Controller cung cấp các API xác thực/ủy quyền dựa trên JWT.
 *
 * Nhóm endpoint chính:
 * - /login, /token: đăng nhập để lấy JWT (access token)
 * - /register: đăng ký tài khoản và trả về JWT
 * - /introspect: kiểm tra token còn hợp lệ/chưa bị thu hồi
 * - /refresh: làm mới token (cấp token mới, token cũ có thể bị thu hồi)
 * - /logout: thu hồi token hiện tại (lưu jti vào bảng invalidated_tokens)
 */
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/token")
    ApiResponse<AuthenticationResponse> authenticate(@RequestBody AuthenticationRequest request) {
        // Endpoint đăng nhập (trả về token trong ApiResponse)
        var result = authService.login(request);
        return ApiResponse.<AuthenticationResponse>builder().result(result).build();
    }

    @PostMapping("/introspect")
    ApiResponse<IntrospectResponse> introspect(@RequestBody IntrospectRequest request)
            throws ParseException, JOSEException {
        // Trả về valid=true/false để phục vụ kiểm tra token (CustomJwtDecoder cũng dùng luồng này)
        var result = authService.introspect(request);
        return ApiResponse.<IntrospectResponse>builder().result(result).build();
    }

    @PostMapping("/register")
    public ApiResponse<AuthenticationResponse> register(@RequestBody RegisterRequest request) {
        // Đăng ký tài khoản và trả về token luôn để client đăng nhập ngay
        return ApiResponse.<AuthenticationResponse>builder()
                .result(authService.register(request))
                .build();
    }

    @PostMapping("/login")
    public ApiResponse<AuthenticationResponse> login(@RequestBody AuthenticationRequest request) {
        // Đăng nhập (alias của /token)
        return ApiResponse.<AuthenticationResponse>builder()
                .result(authService.login(request))
                .build();
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @RequestBody(required = false) LogoutRequest request) {
        String token = null;

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            token = authorizationHeader.substring(7).trim();
        } else if (request != null && request.getToken() != null) {
            token = request.getToken().trim();
        }

        try {
            authService.logout(LogoutRequest.builder().token(token).build());
        } catch (Exception ignored) {
        }

        return ResponseEntity.noContent().build();
    }
}
