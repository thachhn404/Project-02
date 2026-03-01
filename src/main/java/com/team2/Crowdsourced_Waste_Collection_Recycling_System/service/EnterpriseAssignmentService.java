package com.team2.Crowdsourced_Waste_Collection_Recycling_System.service;

import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response.AssignCollectorResponse;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response.EligibleCollectorResponse;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response.RequestPreviewResponse;

import java.util.List;

public interface EnterpriseAssignmentService {
    AssignCollectorResponse assignCollector(Integer enterpriseId, Integer requestId, Integer collectorId);

    AssignCollectorResponse assignCollectorByReportCode(Integer enterpriseId, String reportCode, Integer collectorId);

    List<EligibleCollectorResponse> findEligibleCollectors(Integer enterpriseId, Integer requestId, Double radiusKm);

    RequestPreviewResponse getRequestPreview(Integer enterpriseId, Integer requestId);
}
