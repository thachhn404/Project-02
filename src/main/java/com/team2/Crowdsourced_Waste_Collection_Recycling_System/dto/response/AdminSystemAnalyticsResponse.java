package com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminSystemAnalyticsResponse {
    
    private ReportStats reportStats;
    private List<WasteTypeStats> wasteTypeStats;
    private CitizenActivityStats citizenActivityStats;
    private CollectorActivityStats collectorActivityStats;
    private List<CollectorPerformanceStats> collectorPerformanceStats;
    private RewardStats rewardStats;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReportStats {
        private long totalRequests;
        private long pending;
        private long accepted;
        private long rejected;
        private long assigned;
        private long collected;
        private long completed;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WasteTypeStats {
        private String categoryName;
        private BigDecimal totalWeightKg;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CitizenActivityStats {
        private long totalCitizens;
        private long totalReportsCreated;
        private long totalPointsEarned;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CollectorActivityStats {
        private long totalCollectors;
        private long totalRequestsCompleted;
        private BigDecimal totalWasteCollected;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CollectorPerformanceStats {
        private Integer collectorId;
        private String collectorName;
        private long requestsCompleted;
        private BigDecimal totalWeightCollected;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RewardStats {
        private long totalPointsDistributed;
        private double averagePointsPerCitizen;
        private List<MonthlyPointStats> pointsDistributedPerMonth;
        private List<CitizenPointStats> topCitizens;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MonthlyPointStats {
        private int year;
        private int month;
        private long points;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CitizenPointStats {
        private Integer citizenId;
        private String fullName;
        private long totalPoints;
    }
}
