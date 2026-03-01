package com.team2.Crowdsourced_Waste_Collection_Recycling_System.enums;

public enum VehicleType {
    CAR,
    TRUCK,
    MOTORBIKE;

    public static VehicleType fromString(String value) {
        if (value == null) return null;
        String v = value.trim().toUpperCase();
        if (v.equals("MOTORCYCLE")) v = "MOTORBIKE";
        return switch (v) {
            case "CAR" -> CAR;
            case "TRUCK" -> TRUCK;
            case "MOTORBIKE" -> MOTORBIKE;
            default -> null;
        };
    }
}
