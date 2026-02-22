package com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response;

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
public class WasteCategoryResponse {
    Integer id;
    String name;
    String unit;
    BigDecimal pointPerUnit;
}

