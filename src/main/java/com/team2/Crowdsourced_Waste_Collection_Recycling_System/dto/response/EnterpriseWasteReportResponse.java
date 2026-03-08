package com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
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
public class EnterpriseWasteReportResponse {
    Integer id;
    String reportCode;
    Integer collectionRequestId;
    String status;
    @JsonProperty("submit_by")
    String submitBy;
    String wasteType;
    String description;
    String address;
    java.math.BigDecimal latitude;
    java.math.BigDecimal longitude;
    String images;
    List<String> imageUrls;
    List<WasteCategoryResponse> categories;
    LocalDateTime createdAt;
}
