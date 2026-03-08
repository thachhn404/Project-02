package com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response;

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
public class CollectorWasteVolumeStatsResponse {
    Integer year;
    BigDecimal totalWeightKg;
    Long totalRequests;
    List<CollectorMonthlyWasteVolumeResponse> byMonth;
    List<CollectorQuarterlyWasteVolumeResponse> byQuarter;
}

