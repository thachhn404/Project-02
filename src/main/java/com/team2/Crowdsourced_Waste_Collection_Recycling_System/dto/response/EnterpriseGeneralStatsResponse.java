package com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EnterpriseGeneralStatsResponse {
    Map<String, Long> reportsByStatus;
    Map<String, BigDecimal> wasteWeightByType;
    List<CollectorPerformanceSummary> collectorPerformance;
    PointDistributionStats pointStats;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class CollectorPerformanceSummary {
        Integer collectorId;
        String fullName;
        Long requestsCompleted;
        BigDecimal totalWeight;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class PointDistributionStats {
        Long totalPointsDistributed;
        Map<String, Long> pointsDistributedPerMonth; // "YYYY-MM" -> Points
    }
}
