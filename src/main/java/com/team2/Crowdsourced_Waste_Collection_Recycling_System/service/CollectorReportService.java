package com.team2.Crowdsourced_Waste_Collection_Recycling_System.service;

import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response.CollectorReportResponse;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response.ReportCollectorResponse;

import java.util.List;

public interface CollectorReportService {
    CollectorReportResponse getReportByCollectionRequest(Integer requestId, Integer collectorId);
    List<CollectorReportResponse> getReportsByCollector(Integer collectorId);
    CollectorReportResponse getReportById(Integer reportId, Integer collectorId);
    ReportCollectorResponse getCreateReport(Integer requestId, Integer collectorId);
}
