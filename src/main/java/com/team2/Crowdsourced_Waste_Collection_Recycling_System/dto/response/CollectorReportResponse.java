package com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response;

import com.team2.Crowdsourced_Waste_Collection_Recycling_System.enums.CollectorReportStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CollectorReportResponse {
    private Integer id;
    private String reportCode;
    private Integer collectionRequestId;
    private Integer collectorId;
    private CollectorReportStatus status;
    private String collectorNote;
    private Integer totalPoint;
    private LocalDateTime collectedAt;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private LocalDateTime createdAt;
    private List<String> imageUrls;
    // You might want to include items here as well if needed
    // private List<CollectorReportItemResponse> items; 
}
