package com.team2.Crowdsourced_Waste_Collection_Recycling_System.enums;

public enum CollectorStatus {
  //Co the nhan nhiem vu moi
    AVAILABLE,

    //dang lam viec , san sang nhan nhiem vu
    ACTIVE,

    //tam nghi khong nhan nhiem vu
    INACTIVE,

    //dinh chi do vi pham
    SUSPEND;

 //kiem tra collector co nhan nhiem vu khong
    public boolean canAcceptTask() {
        return this == AVAILABLE || this == ACTIVE;
    }

//chuyen doi string sang enum
    public static CollectorStatus fromString(String status) {
        if (status == null)
            return null;
        return CollectorStatus.valueOf(status.toUpperCase());
    }
}
