package com.team2.Crowdsourced_Waste_Collection_Recycling_System.controller;

import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.request.LoginRequest;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.request.LogoutRequest;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.request.RegisterRequest;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response.AuthenResponse;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) { this.authService = authService; }

    @PostMapping("/register")
    public ResponseEntity<AuthenResponse> register(@RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthenResponse> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestBody(required = false) LogoutRequest request) {
        authService.logout();
        return ResponseEntity.noContent().build();
    }
}