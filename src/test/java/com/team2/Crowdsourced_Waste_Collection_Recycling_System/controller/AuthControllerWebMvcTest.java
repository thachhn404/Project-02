package com.team2.Crowdsourced_Waste_Collection_Recycling_System.controller;

import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response.AuthenResponse;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
public class AuthControllerWebMvcTest {

    @MockBean
    private AuthService authService;

    @MockBean
    private com.team2.Crowdsourced_Waste_Collection_Recycling_System.config.security.JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private com.team2.Crowdsourced_Waste_Collection_Recycling_System.config.security.SecurityConfig securityConfig;

    @MockBean
    private com.team2.Crowdsourced_Waste_Collection_Recycling_System.config.security.JwtService jwtService;

    private final MockMvc mockMvc;

    public AuthControllerWebMvcTest(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
    }

    @Test
    void login_returns_200() throws Exception {
        when(authService.login(any())).thenReturn(new AuthenResponse("token", "Bearer", 1000L, null));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"a@b.com\",\"password\":\"pw\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void register_returns_200() throws Exception {
        when(authService.register(any())).thenReturn(new AuthenResponse("token", "Bearer", 1000L, null));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"a@b.com\",\"password\":\"pw\",\"fullName\":\"A\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void logout_returns_204() throws Exception {
        mockMvc.perform(post("/api/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isNoContent());
    }
}
