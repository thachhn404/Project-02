package com.team2.Crowdsourced_Waste_Collection_Recycling_System.service;

import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.request.CreateCollectorReportRequest;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response.CollectorReportResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface CollectorReportService {
    CollectorReportResponse createCollectorReport(CreateCollectorReportRequest request, List<MultipartFile> images, Integer collectorId);
    CollectorReportResponse getReportByCollectionRequest(Integer requestId, Integer collectorId);
    List<CollectorReportResponse> getReportsByCollector(Integer collectorId);
    CollectorReportResponse getReportById(Integer reportId, Integer collectorId);
}
