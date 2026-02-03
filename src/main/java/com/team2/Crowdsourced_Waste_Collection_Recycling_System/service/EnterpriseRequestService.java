package com.team2.Crowdsourced_Waste_Collection_Recycling_System.service;

public interface EnterpriseRequestService {
    Integer acceptRequest(Integer enterpriseId, String requestCode);
    void acceptRequest(Integer enterpriseId, Integer collectionRequestId);
}
