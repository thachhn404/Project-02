package com.team2.Crowdsourced_Waste_Collection_Recycling_System.controller.enterprise;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.request.UpdateVoucherRequest;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response.CloudinaryResponse;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.Voucher;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.reward.VoucherRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.service.CloudinaryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class EnterpriseVoucherControllerTest {
    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired VoucherRepository voucherRepository;
    @MockBean CloudinaryService cloudinaryService;

    @BeforeEach
    void setup() {
        voucherRepository.deleteAll();
    }

    @Test
    void list_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/enterprise/vouchers"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void create_missingEnterpriseIdClaim_returns403() throws Exception {
        mockMvc.perform(multipart("/api/enterprise/vouchers")
                        .param("title", "Voucher A")
                        .param("pointsRequired", "10")
                        .param("active", "true")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ENTERPRISE"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void crud_happyPath() throws Exception {
        when(cloudinaryService.uploadImage(ArgumentMatchers.any(), ArgumentMatchers.eq("vouchers")))
                .thenReturn(CloudinaryResponse.builder().publicId("pid1").url("https://res.cloudinary.com/demo/banner.png").build())
                .thenReturn(CloudinaryResponse.builder().publicId("pid2").url("https://res.cloudinary.com/demo/logo.png").build());

        MockMultipartFile banner = new MockMultipartFile(
                "banner", "banner.png", "image/png", new byte[]{1, 2, 3}
        );
        MockMultipartFile logo = new MockMultipartFile(
                "logo", "logo.png", "image/png", new byte[]{4, 5, 6}
        );

        String createResponse = mockMvc.perform(multipart("/api/enterprise/vouchers")
                        .file(banner)
                        .file(logo)
                        .param("title", "Voucher A")
                        .param("valueDisplay", "10% OFF")
                        .param("pointsRequired", "10")
                        .param("validUntil", LocalDate.now().plusDays(30).toString())
                        .param("active", "true")
                        .param("remainingStock", "5")
                        .param("terms", "T1", "T2")
                        .with(jwt().jwt(j -> j.claim("enterpriseId", 1))
                                .authorities(new SimpleGrantedAuthority("ROLE_ENTERPRISE"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.title", is("Voucher A")))
                .andExpect(jsonPath("$.result.pointsRequired", is(10)))
                .andExpect(jsonPath("$.result.bannerUrl", is("https://res.cloudinary.com/demo/banner.png")))
                .andExpect(jsonPath("$.result.logoUrl", is("https://res.cloudinary.com/demo/logo.png")))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Integer id = objectMapper.readTree(createResponse).path("result").path("id").asInt();
        String expectedVoucherCode = String.format("V%03d", id);

        mockMvc.perform(get("/api/enterprise/vouchers/{id}", id)
                        .with(jwt().jwt(j -> j.claim("enterpriseId", 1))
                                .authorities(new SimpleGrantedAuthority("ROLE_ENTERPRISE"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.id", is(id)))
                .andExpect(jsonPath("$.result.voucherCode", is(expectedVoucherCode)))
                .andExpect(jsonPath("$.result.valueDisplay", is("10% OFF")));

        UpdateVoucherRequest update = UpdateVoucherRequest.builder()
                .pointsRequired(20)
                .active(false)
                .remainingStock(3)
                .terms(List.of("T3"))
                .build();

        mockMvc.perform(put("/api/enterprise/vouchers/{id}", id)
                        .with(jwt().jwt(j -> j.claim("enterpriseId", 1))
                                .authorities(new SimpleGrantedAuthority("ROLE_ENTERPRISE")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.pointsRequired", is(20)))
                .andExpect(jsonPath("$.result.active", is(false)))
                .andExpect(jsonPath("$.result.voucherCode", is(expectedVoucherCode)))
                .andExpect(jsonPath("$.result.remainingStock", is(3)));

        mockMvc.perform(delete("/api/enterprise/vouchers/{id}", id)
                        .with(jwt().jwt(j -> j.claim("enterpriseId", 1))
                                .authorities(new SimpleGrantedAuthority("ROLE_ENTERPRISE"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("Xóa voucher thành công")));

        Voucher saved = voucherRepository.findById(id).orElseThrow();
        org.junit.jupiter.api.Assertions.assertEquals(Boolean.FALSE, saved.getActive());
        org.junit.jupiter.api.Assertions.assertEquals("https://res.cloudinary.com/demo/banner.png", saved.getBannerUrl());
        org.junit.jupiter.api.Assertions.assertEquals("https://res.cloudinary.com/demo/logo.png", saved.getLogoUrl());
        org.junit.jupiter.api.Assertions.assertEquals("pid1", saved.getBannerPublicId());
        org.junit.jupiter.api.Assertions.assertEquals("pid2", saved.getLogoPublicId());
    }
}
