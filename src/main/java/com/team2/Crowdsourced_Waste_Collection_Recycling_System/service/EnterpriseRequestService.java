package com.team2.Crowdsourced_Waste_Collection_Recycling_System.service;

public interface EnterpriseRequestService {
    /**
     * Enterprise accept một WasteReport và tự động tạo CollectionRequest
     * 
     * @param enterpriseId ID của enterprise
     * @param reportCode   Mã của WasteReport cần accept
     * @return ID của CollectionRequest đã tạo
     */
    Integer acceptWasteReport(Integer enterpriseId, String reportCode);

    Integer acceptWasteReport(Integer enterpriseId, String reportCode, java.math.BigDecimal estimatedWeight);

    void rejectWasteReport(Integer enterpriseId, String reportCode, String reason);
}
