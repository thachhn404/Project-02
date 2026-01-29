package com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.request;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IntrospectRequest {
    private String token;
}
