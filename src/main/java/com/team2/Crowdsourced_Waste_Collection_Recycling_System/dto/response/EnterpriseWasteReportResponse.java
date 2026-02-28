package com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EnterpriseWasteReportResponse {
    Integer id;
    String reportCode;
    String status;
    String wasteType;
    String description;
    String address;
    java.math.BigDecimal latitude;
    java.math.BigDecimal longitude;
    String images;
    LocalDateTime createdAt;
}
