package com.team2.Crowdsourced_Waste_Collection_Recycling_System.enums;

public enum WasteReportStatus {

    PENDING,
//enterprise chap nhan chuan bi phan cho collector
    ACCEPTED_ENTERPRISE,

    REASSIGN,

//da phan cong cho collector
    ASSIGNED,

 //collector chap nhan
    ACCEPTED_COLLECTOR,
    ON_THE_WAY,
    COLLECTED,
    //bi tu choi neu khong hop le
    REJECTED,
    TIMED_OUT;
    public static WasteReportStatus fromString(String status) {
        if (status == null)
            return null;
        return WasteReportStatus.valueOf(status.toUpperCase());
    }
}
