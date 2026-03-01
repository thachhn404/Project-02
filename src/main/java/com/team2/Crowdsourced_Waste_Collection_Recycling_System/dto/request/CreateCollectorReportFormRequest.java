package com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import jakarta.validation.constraints.DecimalMin;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateCollectorReportFormRequest {
    private String collectorNote;
    private String address;
    private Double latitude;
    private Double longitude;
    @DecimalMin(value = "0.00")
    private BigDecimal actualWeightKg;
    private List<CollectorReportItemRequest> items;
}
