package com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CitizenReportResultResponse {
    Integer reportId;
    String reportCode;
    String status;
    Integer totalPoint;
    String classificationResult;
    LocalDateTime collectedAt;
    List<CitizenReportResultItemResponse> items;
}

