package com.team2.Crowdsourced_Waste_Collection_Recycling_System.service;

import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.request.CreateCollectorReportRequest;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response.CollectorReportResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface CollectorReportService {

    CollectorReportResponse createCollectorReport(CreateCollectorReportRequest request, List<MultipartFile> images, Integer collectorId);

    CollectorReportResponse getReportById(Integer reportId, Integer collectorId);


    Page<CollectorReportResponse> getReportsByCollector(Integer collectorId, Pageable pageable);

    CollectorReportResponse getReportByCollectionRequest(Integer collectionRequestId, Integer collectorId);

}
