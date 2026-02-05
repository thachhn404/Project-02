package com.team2.Crowdsourced_Waste_Collection_Recycling_System.service;

import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.request.CreateWasteReportRequest;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response.WasteReportResponse;

import java.util.List;

public interface WasteReportService {
    WasteReportResponse createReport(CreateWasteReportRequest request, String citizenEmail);

    List<WasteReportResponse> getMyReports(String citizenEmail);

    WasteReportResponse getMyReportById(Integer reportId, String citizenEmail);
}
