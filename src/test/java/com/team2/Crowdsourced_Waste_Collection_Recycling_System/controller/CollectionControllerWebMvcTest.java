package com.team2.Crowdsourced_Waste_Collection_Recycling_System.controller;

import com.team2.Crowdsourced_Waste_Collection_Recycling_System.config.CustomJwtDecoder;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.config.SecurityConfig;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.controller.collector.CollectionController;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response.CollectorTaskResponse;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.enums.CollectorReportStatus;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.service.CollectorReportService;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.service.CollectorService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.core.authority.AuthorityUtils.createAuthorityList;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = CollectionController.class)
@Import(SecurityConfig.class)
class CollectionControllerWebMvcTest {
    @Autowired
    MockMvc mockMvc;

    @org.springframework.boot.test.mock.mockito.MockBean
    CollectorService collectorService;

    @org.springframework.boot.test.mock.mockito.MockBean
    CollectorReportService collectorReportService;

    @org.springframework.boot.test.mock.mockito.MockBean
    CustomJwtDecoder customJwtDecoder;

    @Test
    void getTasks_default_calls_active_tasks_query() throws Exception {
        when(collectorService.getTasks(eq(200), isNull(), eq(false), any()))
                .thenReturn(new PageImpl<>(
                        List.of(CollectorTaskResponse.builder()
                                .id(10)
                                .requestCode("REQ001")
                                .status("assigned")
                                .build()),
                        PageRequest.of(0, 10),
                        1));

        mockMvc.perform(get("/api/collector/collections/tasks")
                        .with(jwt().authorities(createAuthorityList("ROLE_COLLECTOR"))
                                .jwt(j -> j.claim("collectorId", 200))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.content[0].id").value(10))
                .andExpect(jsonPath("$.result.content[0].requestCode").value("REQ001"))
                .andExpect(jsonPath("$.result.content[0].status").value("assigned"));

        verify(collectorService).getTasks(eq(200), isNull(), eq(false), any());
    }

    @Test
    void getTasks_all_true_calls_all_tasks_query() throws Exception {
        when(collectorService.getTasks(eq(200), isNull(), eq(true), any()))
                .thenReturn(new PageImpl<>(
                        List.of(CollectorTaskResponse.builder()
                                .id(11)
                                .requestCode("REQ002")
                                .status("collected")
                                .build()),
                        PageRequest.of(0, 10),
                        1));

        mockMvc.perform(get("/api/collector/collections/tasks")
                        .queryParam("all", "true")
                        .with(jwt().authorities(createAuthorityList("ROLE_COLLECTOR"))
                                .jwt(j -> j.claim("collectorId", 200))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.content[0].id").value(11));

        verify(collectorService).getTasks(eq(200), isNull(), eq(true), any());
    }

    @Test
    void getTasks_status_param_calls_status_query() throws Exception {
        when(collectorService.getTasks(eq(200), eq("on_the_way"), eq(false), any()))
                .thenReturn(new PageImpl<>(
                        List.of(CollectorTaskResponse.builder()
                                .id(12)
                                .requestCode("REQ003")
                                .status("on_the_way")
                                .build()),
                        PageRequest.of(0, 10),
                        1));

        mockMvc.perform(get("/api/collector/collections/tasks")
                        .queryParam("status", "on_the_way")
                        .with(jwt().authorities(createAuthorityList("ROLE_COLLECTOR"))
                                .jwt(j -> j.claim("collectorId", 200))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.content[0].status").value("on_the_way"));

        verify(collectorService).getTasks(eq(200), eq("on_the_way"), eq(false), any());
    }

    @Test
    void acceptTask_calls_service_and_returns_expected_status() throws Exception {
        mockMvc.perform(post("/api/collector/collections/{id}/accept", 100)
                        .with(jwt().authorities(createAuthorityList("ROLE_COLLECTOR"))
                                .jwt(j -> j.claim("collectorId", 200))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.collectionRequestId").value(100))
                .andExpect(jsonPath("$.result.status").value("accepted_collector"));

        verify(collectorService).acceptTask(100, 200);
        verifyNoMoreInteractions(collectorService);
    }

    @Test
    void startTask_calls_service_and_returns_expected_status() throws Exception {
        mockMvc.perform(post("/api/collector/collections/{id}/start", 101)
                        .with(jwt().authorities(createAuthorityList("ROLE_COLLECTOR"))
                                .jwt(j -> j.claim("collectorId", 200))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.collectionRequestId").value(101))
                .andExpect(jsonPath("$.result.status").value("on_the_way"));

        verify(collectorService).startTask(101, 200);
        verifyNoMoreInteractions(collectorService);
    }

    @Test
    void rejectTask_missing_collectorId_claim_returns_403() throws Exception {
        mockMvc.perform(post("/api/collector/collections/{id}/reject", 102)
                        .contentType("application/json")
                        .content("{\"reason\":\"bận\"}")
                        .with(jwt().authorities(createAuthorityList("ROLE_COLLECTOR"))))
                .andExpect(status().isForbidden());

        verifyNoInteractions(collectorService);
    }

    @Test
    void completeTask_calls_service_and_returns_expected_status() throws Exception {
        var response = com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response.CollectorReportResponse.builder()
                .reportId(999)
                .collectionRequestId(103)
                .collectorId(200)
                .collectorName("Test Collector")
                .status(CollectorReportStatus.COMPLETED)
                .collectorNote("done")
                .imageUrls(List.of("https://example.com/1.png"))
                .build();

        when(collectorReportService.createCollectorReport(any(), anyList(), eq(200))).thenReturn(response);

        var image = new org.springframework.mock.web.MockMultipartFile(
                "images",
                "photo.png",
                "image/png",
                "x".getBytes()
        );

        mockMvc.perform(multipart("/api/collector/collections/{id}/complete", 103)
                        .file(image)
                        .file(new org.springframework.mock.web.MockMultipartFile(
                                "report",
                                "report.json",
                                "application/json",
                                """
                                {
                                  "wasteType":"RECYCLABLE",
                                  "collectorNote":"done",
                                  "items":[
                                    {"categoryId":1,"quantity":2.0}
                                  ]
                                }
                                """.getBytes()
                        ))
                        .with(jwt().authorities(createAuthorityList("ROLE_COLLECTOR"))
                                .jwt(j -> j.claim("collectorId", 200))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.reportId").value(999))
                .andExpect(jsonPath("$.result.collectionRequestId").value(103))
                .andExpect(jsonPath("$.result.collectorId").value(200));

        verify(collectorReportService).createCollectorReport(any(), anyList(), eq(200));
    }

    @Test
    void markCollected_calls_service_and_returns_expected_status() throws Exception {
        mockMvc.perform(post("/api/collector/collections/{id}/collected", 104)
                        .with(jwt().authorities(createAuthorityList("ROLE_COLLECTOR"))
                                .jwt(j -> j.claim("collectorId", 200))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.collectionRequestId").value(104))
                .andExpect(jsonPath("$.result.status").value("collected"));

        verify(collectorService).completeTask(104, 200);
        verifyNoMoreInteractions(collectorService);
    }

    @Test
    void getReportByCollectionRequest_whenMissing_returns_404() throws Exception {
        when(collectorReportService.getReportByCollectionRequest(105, 200)).thenReturn(null);

        mockMvc.perform(get("/api/collector/collections/{id}/report", 105)
                        .with(jwt().authorities(createAuthorityList("ROLE_COLLECTOR"))
                                .jwt(j -> j.claim("collectorId", 200))))
                .andExpect(status().isNotFound());
    }

}
