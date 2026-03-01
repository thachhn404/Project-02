package com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CollectorReportItemRequest {
    private Integer categoryId;
    private String categoryName;
    private BigDecimal quantity;
}
