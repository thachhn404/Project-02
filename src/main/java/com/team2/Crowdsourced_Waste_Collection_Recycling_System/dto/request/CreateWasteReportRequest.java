package com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.web.multipart.MultipartFile;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateWasteReportRequest {
    MultipartFile image;
    Double latitude;
    Double longitude;
    String description;

    @NotNull(message = "Khối lượng rác là bắt buộc")
    @DecimalMin(value = "0.1", message = "Khối lượng tối thiểu là 0.1kg")
    @DecimalMax(value = "1000.0", message = "Khối lượng tối đa là 1000kg")
    @Digits(integer = 10, fraction = 2, message = "Khối lượng chỉ được 2 chữ số thập phân")
    BigDecimal estimatedWeight; // Required, kg

    @NotEmpty(message = "Phải chọn ít nhất 1 loại rác")
    @Size(min = 1, max = 3, message = "Chỉ được chọn tối đa 3 loại rác")
    List<String> wasteTypes; // HOUSEHOLD, RECYCLABLE, HAZARDOUS
}
