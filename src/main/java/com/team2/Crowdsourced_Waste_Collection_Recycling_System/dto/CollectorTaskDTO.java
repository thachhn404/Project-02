package com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto;

import java.time.LocalDateTime;
import java.math.BigDecimal;

public interface CollectorTaskDTO {
    Integer getCollectionRequestId();
    String getRequestCode();
    String getPriority();
    LocalDateTime getAssignedAt();
    BigDecimal getLatitude();
    BigDecimal getLongitude();
    String getAddress();
    String getWard();
    String getCity();
    String getWasteTypeCode();
    String getWasteTypeName();
    String getWasteDescription();
    LocalDateTime getEstimatedArrival();
}
