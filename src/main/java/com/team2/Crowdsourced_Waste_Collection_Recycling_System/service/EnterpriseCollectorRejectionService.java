package com.team2.Crowdsourced_Waste_Collection_Recycling_System.service;

import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response.EnterpriseCollectorRejectionResponse;

import java.util.List;

public interface EnterpriseCollectorRejectionService {
    List<EnterpriseCollectorRejectionResponse> getCollectorRejections(Integer enterpriseId);
}

