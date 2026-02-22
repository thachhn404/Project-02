package com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.request;

import com.team2.Crowdsourced_Waste_Collection_Recycling_System.enums.CollectionRequestStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateTaskStatusRequest {
    private String status;
}
