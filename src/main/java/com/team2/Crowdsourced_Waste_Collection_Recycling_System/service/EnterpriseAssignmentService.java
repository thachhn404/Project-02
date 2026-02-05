package com.team2.Crowdsourced_Waste_Collection_Recycling_System.service;

import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response.AssignCollectorResponse;

public interface EnterpriseAssignmentService {
    AssignCollectorResponse assignCollector(Integer enterpriseId, String requestCode, Integer collectorId);
}
