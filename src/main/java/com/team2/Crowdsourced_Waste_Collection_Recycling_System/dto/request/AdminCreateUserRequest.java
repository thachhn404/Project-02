package com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.request;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AdminCreateUserRequest {
    String email;
    String password;
    String fullName;
    String phone;

    // Supported: CITIZEN, COLLECTOR, ENTERPRISE
    String roleCode;

    // Required when roleCode = COLLECTOR
    Integer enterpriseId;
    String employeeCode;
    String vehicleType;
    String vehiclePlate;

    // Required when roleCode = ENTERPRISE
    String enterpriseName;
    String enterpriseAddress;
    String enterprisePhone;
    String enterpriseEmail;

    // Optional citizen profile fields
    String citizenAddress;
    String citizenWard;
    String citizenCity;
}
