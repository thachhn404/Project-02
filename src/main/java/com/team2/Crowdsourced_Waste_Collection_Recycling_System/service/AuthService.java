package com.team2.Crowdsourced_Waste_Collection_Recycling_System.service;

import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.request.LoginRequest;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.request.RegisterRequest;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response.AuthenResponse;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response.TokenResponse;

/**
 * Interface định nghĩa các nghiệp vụ xác thực người dùng.
 * Bao gồm đăng ký, đăng nhập và đăng xuất.
 */
public interface AuthService {
    

    AuthenResponse register(RegisterRequest request);
    

    AuthenResponse login(LoginRequest request);
    

    void logout();


    TokenResponse refreshToken(String refreshToken);
}
