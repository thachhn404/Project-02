package com.team2.Crowdsourced_Waste_Collection_Recycling_System.controller;

import com.team2.Crowdsourced_Waste_Collection_Recycling_System.config.SecurityConfig;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response.ApiResponse;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response.AuthenticationResponse;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;

/**
 * Test cho AuthController sử dụng MockMvc.
 * Loại bỏ cấu hình Security để tập trung test logic Controller.
 */
@WebMvcTest(controllers = AuthController.class, 
    excludeAutoConfiguration = SecurityAutoConfiguration.class,
    excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {
        SecurityConfig.class
    }))
@AutoConfigureMockMvc(addFilters = false)
public class AuthControllerWebMvcTest {

    @MockBean
    private AuthService authService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void login_returns_200() throws Exception {
        when(authService.login(any())).thenReturn(new AuthenticationResponse("token", true, null));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"a@b.com\",\"password\":\"pw\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void register_returns_200() throws Exception {
        when(authService.register(any())).thenReturn(new AuthenticationResponse("token", true, null));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"a@b.com\",\"password\":\"pw\",\"fullName\":\"A\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void logout_returns_204() throws Exception {
        mockMvc.perform(post("/api/auth/logout")
                        .header("Authorization", "Bearer dummy-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isNoContent());
    }

    @Test
    void logout_accepts_body_token_returns_204() throws Exception {
        mockMvc.perform(post("/api/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"token\":\"dummy-token\"}"))
                .andExpect(status().isNoContent());
    }
}
