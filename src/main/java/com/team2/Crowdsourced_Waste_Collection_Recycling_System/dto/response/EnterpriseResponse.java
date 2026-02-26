package com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EnterpriseResponse {
    Integer id;
    String name;
    String address;
    String ward;
    String city;
    String phone;
    String email;
    String licenseNumber;
    String taxCode;
    BigDecimal capacityKgPerDay;
    String supportedWasteTypeCodes;
    String serviceWards;
    String serviceCities;
    String status;
    BigDecimal totalCollectedWeight;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}

