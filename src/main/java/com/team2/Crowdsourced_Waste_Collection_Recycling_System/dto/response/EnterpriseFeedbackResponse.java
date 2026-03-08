package com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnterpriseFeedbackResponse {
    private Integer id;
    private String feedbackCode;
    private String subject;
    private String content;
    private String resolution;
    private String status;
    private String citizenName;
    private String citizenEmail;
    private LocalDateTime createdAt;
    private Integer collectionRequestId;
    private CollectorReportResponse collectorReport;
}
