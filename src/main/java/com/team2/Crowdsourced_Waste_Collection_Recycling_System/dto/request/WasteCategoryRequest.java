package com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.request;

import com.team2.Crowdsourced_Waste_Collection_Recycling_System.enums.WasteUnit;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class WasteCategoryRequest {

    @NotBlank(message = "Tên loại rác không được để trống")
    String name;

    String description;

    @NotNull(message = "Đơn vị không được để trống")
    WasteUnit unit;

    @NotNull(message = "Điểm/đơn vị không được để trống")
    @Positive(message = "Điểm/đơn vị phải lớn hơn 0")
    BigDecimal pointPerUnit;
}
