package com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
public class CreateCollectorReportRequest {
    @Size(min = 1, message = "Cần ít nhất 1 ảnh")
    List<MultipartFile> images;

    @Size(min = 1, message = "Phải chọn ít nhất 1 danh mục")
    List<Integer> categoryIds;

    List<BigDecimal> quantities;

    @NotBlank(message = "Mô tả thực tế là bắt buộc")
    @Size(max = 1000, message = "Ghi chú không được vượt quá 1000 ký tự.")
    String collectorNote;

    @NotNull(message = "Vĩ độ là bắt buộc")
    @DecimalMin(value = "-90.0", message = "Vĩ độ phải nằm trong khoảng [-90, 90]")
    @DecimalMax(value = "90.0", message = "Vĩ độ phải nằm trong khoảng [-90, 90]")
    Double latitude;

    @NotNull(message = "Kinh độ là bắt buộc")
    @DecimalMin(value = "-180.0", message = "Kinh độ phải nằm trong khoảng [-180, 180]")
    @DecimalMax(value = "180.0", message = "Kinh độ phải nằm trong khoảng [-180, 180]")
    Double longitude;

    Integer verificationRate;
}
