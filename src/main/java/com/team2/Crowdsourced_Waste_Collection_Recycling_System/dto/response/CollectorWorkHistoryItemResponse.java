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
public class CollectorWorkHistoryItemResponse {
    Integer collectionRequestId;
    String requestCode;
    String status;
    String address;
    String wasteTypeCode;
    String wasteTypeName;
    Integer enterpriseId;
    String enterpriseName;
    LocalDateTime startedAt;
    LocalDateTime collectedAt;
    LocalDateTime completedAt;
    LocalDateTime updatedAt;
}
