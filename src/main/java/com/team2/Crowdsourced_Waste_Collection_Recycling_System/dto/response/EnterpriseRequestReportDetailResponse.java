package com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public classEnterpriseRequestReportDetailResponse {
    Integer requestId;
    String requestCode;
    String requestStatus;
    LocalDateTime assignedAt;
    LocalDateTime acceptedAt;
    LocalDateTime startedAt;
    LocalDateTime collectedAt;
    LocalDateTime completedAt;
    BigDecimal actualWeightKg;
    EnterpriseWasteReportResponse wasteReport;
    CollectorReportResponse collectorReport;
}
