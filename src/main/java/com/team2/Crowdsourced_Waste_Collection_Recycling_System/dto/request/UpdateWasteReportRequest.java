package com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateWasteReportRequest {
    List<MultipartFile> images;
    String wasteType;
    List<String> categoryIds;
    List<BigDecimal> quantities;

    @DecimalMin(value = "-90.0", message = "Vĩ độ phải nằm trong khoảng [-90, 90]")
    @DecimalMax(value = "90.0", message = "Vĩ độ phải nằm trong khoảng [-90, 90]")
    Double latitude;

    @DecimalMin(value = "-180.0", message = "Kinh độ phải nằm trong khoảng [-180, 180]")
    @DecimalMax(value = "180.0", message = "Kinh độ phải nằm trong khoảng [-180, 180]")
    Double longitude;
    String address;
    String description;
}
