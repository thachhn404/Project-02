package com.team2.Crowdsourced_Waste_Collection_Recycling_System.enums;

public enum CollectorStatus {
    // Đang online, sẵn sàng nhận nhiệm vụ
    ONLINE,

    // Đang offline, không thể nhận nhiệm vụ
    OFFLINE,

    // Bị đình chỉ
    SUSPEND;

    // Kiểm tra collector có thể nhận nhiệm vụ không
    public boolean canAcceptTask() {
        return this == ONLINE;
    }

    // Chuyển đổi string sang enum
    public static CollectorStatus fromString(String status) {
        if (status == null) {
            return null;
        }
        String normalized = status.trim().toUpperCase();
        if (normalized.isEmpty()) {
            return null;
        }
        return switch (normalized) {
            case "ONLINE", "AVAILABLE", "ACTIVE" -> ONLINE;
            case "OFFLINE", "INACTIVE" -> OFFLINE;
            case "SUSPEND", "SUSPENDED" -> SUSPEND;
            default -> {
                try {
                    yield CollectorStatus.valueOf(normalized);
                } catch (IllegalArgumentException e) {
                    yield null;
                }
            }
        };
    }
}
