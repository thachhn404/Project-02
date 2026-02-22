package com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto;

import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.request.CreateWasteReportRequest;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class CreateWasteReportRequestValidationTest {
    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void latitudeOutOfRange_shouldFailValidation() {
        CreateWasteReportRequest request = CreateWasteReportRequest.builder()
                .images(List.of(new MockMultipartFile("images", "1.jpg", "image/jpeg", new byte[]{1})))
                .wasteType("Recycle")
                .categoryIds(List.of("1"))
                .latitude(106.1002)
                .longitude(108.108)
                .address("32/21 Võ Văn Hát")
                .description("test")
                .build();

        var violations = validator.validate(request);

        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("latitude"));
    }

    @Test
    void validCoordinates_shouldPassValidation() {
        CreateWasteReportRequest request = CreateWasteReportRequest.builder()
                .images(List.of(new MockMultipartFile("images", "1.jpg", "image/jpeg", new byte[]{1})))
                .wasteType("Recycle")
                .categoryIds(List.of("1"))
                .latitude(10.61002)
                .longitude(106.1002)
                .address("32/21 Võ Văn Hát")
                .description("test")
                .build();

        var violations = validator.validate(request);

        assertThat(violations).isEmpty();
    }
}

