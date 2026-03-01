package com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ReportCollectorResponse {
    Integer collectionRequestId;
    Integer wasteCollectionRequestId;
    String wasteReportCode;
    String wasteType;
    String address;
    BigDecimal latitude;
    BigDecimal longitude;
    List<ReportCollectorItemResponse> items;
    List<String> imageUrls;
}
