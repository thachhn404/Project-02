package com.team2.Crowdsourced_Waste_Collection_Recycling_System.enums;

public enum CollectionRequestStatus {

    PENDING,

    //enterpris chap nhan nhung chua phan cong
    ACCEPTED_ENTERPRISE,

//enterprise phan cong cho collector accept
    ASSIGNED,
//collector chap nhan va chuan  bi chuyen trang thai on the way
    ACCEPTED_COLLECTOR,

   //collector di den lay rac
    ON_THE_WAY,

//collector
    COLLECTED,

   //enterprise tu choi bao cao hoac citizen huy bao cao
    REJECTED;


     // Kiểm tra xem status hiện tại có phải là "active task" không
     // Active tasks = ASSIGNED | ACCEPTED_COLLECTOR | ON_THE_WAY

    public boolean isActiveTask() {
        return this == ASSIGNED || this == ACCEPTED_COLLECTOR || this == ON_THE_WAY;
    }

    //kiem tra task da hoan thanh chua
    public boolean isCompleted() {
        return this == COLLECTED;
    }

   //chuyen string sang enum
    public static CollectionRequestStatus fromString(String status) {
        if (status == null)
            return null;
        return CollectionRequestStatus.valueOf(status.toUpperCase());
    }
}
