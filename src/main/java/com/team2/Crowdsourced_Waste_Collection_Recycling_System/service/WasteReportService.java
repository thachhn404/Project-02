package com.team2.Crowdsourced_Waste_Collection_Recycling_System.service;

import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.request.CreateWasteReportRequest;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.request.UpdateWasteReportRequest;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response.CitizenReportResultResponse;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response.WasteCategoryResponse;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response.WasteReportResponse;

import java.util.List;

public interface WasteReportService {
    WasteReportResponse createReport(CreateWasteReportRequest request, String citizenEmail);

    WasteReportResponse updateReport(Integer reportId, UpdateWasteReportRequest request, String citizenEmail);

    void deleteReport(Integer reportId, String citizenEmail);

    List<WasteReportResponse> getMyReports(String citizenEmail);

    WasteReportResponse getMyReportById(Integer reportId, String citizenEmail);

    CitizenReportResultResponse getMyReportResult(Integer reportId, String citizenEmail);

    List<com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response.CitizenRewardHistoryResponse> getRewardHistory(String citizenEmail, java.time.LocalDateTime startDate, java.time.LocalDateTime endDate);

    List<com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response.CitizenLeaderboardResponse> getLeaderboard(String region);

    com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response.ComplaintResponse createComplaint(com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.request.CreateComplaintRequest request, String citizenEmail);

    List<com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response.ComplaintResponse> getComplaints(String citizenEmail);

    List<WasteCategoryResponse> getWasteCategories();
}

