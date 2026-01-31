package com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuthResponse {
    private String token;
    private String email;
    private String role;
    private String message;
    
    // User basic info
    private Integer id;
    private String fullName;
    private String phone;
    
    // Citizen / Enterprise Location
    private String address;
    private String ward;
    private String city;
    
    // Enterprise specific
    private String enterpriseName;
    private String taxCode;
    private String licenseNumber;
    
    // Collector specific
    private String vehicleType;
    private String vehiclePlate;
    private String status;
    
    // Enterprise Admin specific
    private Integer enterpriseId;
}
