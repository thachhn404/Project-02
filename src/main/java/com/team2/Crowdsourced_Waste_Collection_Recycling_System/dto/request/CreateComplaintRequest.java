package com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateComplaintRequest {
    @NotNull(message = "Mã báo cáo không được để trống")
    Integer reportId;

    @NotBlank(message = "Loại khiếu nại không được để trống")
    String type;

    @NotBlank(message = "Nội dung khiếu nại không được để trống")
    String content;
}
