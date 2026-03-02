package com.team2.Crowdsourced_Waste_Collection_Recycling_System.service;

import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response.CollectorReportResponse;

import java.util.List;

public interface EnterpriseCollectorReportService {
    List<CollectorReportResponse> getCollectorReports(Integer enterpriseId);
}
