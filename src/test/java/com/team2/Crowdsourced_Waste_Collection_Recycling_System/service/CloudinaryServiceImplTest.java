package com.team2.Crowdsourced_Waste_Collection_Recycling_System.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.Uploader;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.service.impl.CloudinaryServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CloudinaryServiceImplTest {
    @Mock
    Cloudinary cloudinary;

    @Mock
    Uploader uploader;

    @Captor
    ArgumentCaptor<Map<String, Object>> optionsCaptor;

    @Test
    void uploadImage_withCollectorReportModule_putsCollectorReportFolder() throws Exception {
        when(cloudinary.uploader()).thenReturn(uploader);
        when(uploader.upload(any(byte[].class), optionsCaptor.capture()))
                .thenReturn(Map.of("public_id", "p1", "secure_url", "https://example.com/p1"));

        CloudinaryServiceImpl service = new CloudinaryServiceImpl(
                cloudinary,
                "",
                "cloud",
                "key",
                "secret"
        );

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "a.png",
                "image/png",
                "x".getBytes()
        );

        service.uploadImage(file, "collectorReport");

        assertEquals("collectorReport", optionsCaptor.getValue().get("folder"));
    }

    @Test
    void uploadImage_withLegacyCollectorReportsModule_putsCollectorReportFolder() throws Exception {
        when(cloudinary.uploader()).thenReturn(uploader);
        when(uploader.upload(any(byte[].class), optionsCaptor.capture()))
                .thenReturn(Map.of("public_id", "p2", "secure_url", "https://example.com/p2"));

        CloudinaryServiceImpl service = new CloudinaryServiceImpl(
                cloudinary,
                "",
                "cloud",
                "key",
                "secret"
        );

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "a.png",
                "image/png",
                "x".getBytes()
        );

        service.uploadImage(file, "collector_reports");

        assertEquals("collectorReport", optionsCaptor.getValue().get("folder"));
    }

    @Test
    void uploadImage_withReportAliasModule_putsReportsFolder() throws Exception {
        when(cloudinary.uploader()).thenReturn(uploader);
        when(uploader.upload(any(byte[].class), optionsCaptor.capture()))
                .thenReturn(Map.of("public_id", "p3", "secure_url", "https://example.com/p3"));

        CloudinaryServiceImpl service = new CloudinaryServiceImpl(
                cloudinary,
                "",
                "cloud",
                "key",
                "secret"
        );

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "a.png",
                "image/png",
                "x".getBytes()
        );

        service.uploadImage(file, "report");

        assertEquals("reports", optionsCaptor.getValue().get("folder"));
    }
}
