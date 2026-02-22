package com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response;

import com.team2.Crowdsourced_Waste_Collection_Recycling_System.enums.WasteUnit;
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
public class CollectorReportItemResponse {
    Integer categoryId;
    String categoryName;
    BigDecimal quantity;
    WasteUnit unit;
    Integer point;
}

