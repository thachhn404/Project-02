package com.team2.Crowdsourced_Waste_Collection_Recycling_System.service;

public interface EnterpriseRequestService {
    Integer acceptWasteReport(Integer enterpriseId, String reportCode);

    Integer acceptWasteReport(Integer enterpriseId, String reportCode, java.math.BigDecimal estimatedWeight);

    void rejectWasteReport(Integer enterpriseId, String reportCode, String reason);
}
