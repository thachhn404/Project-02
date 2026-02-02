package com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CollectionRequestActionResponse {
    private Integer collectionRequestId;
    private String status;
    private LocalDateTime actionAt;
}
