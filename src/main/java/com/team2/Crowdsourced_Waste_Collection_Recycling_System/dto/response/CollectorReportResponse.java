package com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response;

import com.team2.Crowdsourced_Waste_Collection_Recycling_System.enums.CollectorReportStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CollectorReportResponse {
    Integer reportId;

    String reportCode;

    Integer collectionRequestId;

    Integer collectorId;

    String collectorName;

    CollectorReportStatus status;

    String collectorNote;

    Integer totalPoint;

    BigDecimal latitude;

    BigDecimal longitude;

    LocalDateTime collectedAt;

    LocalDateTime createdAt;

    List<String> imageUrls;

    List<CollectorReportItemResponse> items;
}
