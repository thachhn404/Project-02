package com.team2.Crowdsourced_Waste_Collection_Recycling_System.service;

import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.request.*;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response.AuthenticationResponse;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response.IntrospectResponse;
import com.nimbusds.jose.JOSEException;
import java.text.ParseException;

/**
 * Interface định nghĩa các nghiệp vụ xác thực người dùng.
 * Bao gồm đăng ký, đăng nhập và đăng xuất.
 */
public interface AuthService {
    
    AuthenticationResponse register(RegisterRequest request);
    
    AuthenticationResponse login(AuthenticationRequest request);
    
    void logout(LogoutRequest request) throws ParseException, JOSEException;

    IntrospectResponse introspect(IntrospectRequest request) throws JOSEException, ParseException;
}
