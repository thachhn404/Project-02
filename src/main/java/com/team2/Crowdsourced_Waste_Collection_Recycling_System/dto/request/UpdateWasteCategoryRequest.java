package com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.request;

import com.team2.Crowdsourced_Waste_Collection_Recycling_System.enums.WasteUnit;
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
public class UpdateWasteCategoryRequest {

    String name;

    String description;

    WasteUnit unit;

    @Positive(message = "Điểm/đơn vị phải lớn hơn 0")
    BigDecimal pointPerUnit;
}
