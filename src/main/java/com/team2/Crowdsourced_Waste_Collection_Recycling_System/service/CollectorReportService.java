package com.team2.Crowdsourced_Waste_Collection_Recycling_System.service;

import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.request.CreateCollectorReportRequest;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response.CollectorReportResponse;

import java.util.List;

public interface CollectorReportService {

    CollectorReportResponse createCollectorReport(CreateCollectorReportRequest request, Integer collectorId);

    CollectorReportResponse getReportById(Integer reportId, Integer collectorId);


    List<CollectorReportResponse> getReportsByCollector(Integer collectorId);

    CollectorReportResponse getReportByCollectionRequest(Integer collectionRequestId, Integer collectorId);

}
