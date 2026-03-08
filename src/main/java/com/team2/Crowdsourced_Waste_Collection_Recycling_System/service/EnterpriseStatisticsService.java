package com.team2.Crowdsourced_Waste_Collection_Recycling_System.service;

import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response.EnterpriseCitizenPointSummaryResponse;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response.EnterpriseWasteVolumeStatsResponse;

import java.util.List;

import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response.EnterpriseGeneralStatsResponse;

public interface EnterpriseStatisticsService {
    EnterpriseWasteVolumeStatsResponse getWasteVolumeStats(Integer enterpriseId, Integer year);

    List<EnterpriseCitizenPointSummaryResponse> getCitizenPointSummaries(Integer enterpriseId, Integer year, Integer quarter, Integer month);

    EnterpriseGeneralStatsResponse getGeneralStats(Integer enterpriseId);
}

