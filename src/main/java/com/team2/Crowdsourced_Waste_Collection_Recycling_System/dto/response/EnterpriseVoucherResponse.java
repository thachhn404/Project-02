package com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EnterpriseVoucherResponse {
    Integer id;
    String voucherCode;
    String bannerUrl;
    String logoUrl;
    String title;
    String valueDisplay;
    Integer pointsRequired;
    LocalDate validUntil;
    Boolean active;
    Integer remainingStock;
    List<String> terms;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
