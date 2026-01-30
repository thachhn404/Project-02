package com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class GeocodeResponse {
    BigDecimal latitude;
    BigDecimal longitude;
    String formattedAddress;
    String address;
    String city;
    String ward;
}
