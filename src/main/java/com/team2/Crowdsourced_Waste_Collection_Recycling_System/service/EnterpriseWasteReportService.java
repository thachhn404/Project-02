package com.team2.Crowdsourced_Waste_Collection_Recycling_System.service;

import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response.EnterpriseWasteReportResponse;

import java.util.List;

public interface EnterpriseWasteReportService {
    List<EnterpriseWasteReportResponse> getPendingReports(Integer enterpriseId);
}
