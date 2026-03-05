package com.team2.Crowdsourced_Waste_Collection_Recycling_System.service;

import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response.CollectorPerformanceStatsResponse;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response.CollectorTaskResponse;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response.CollectorTaskStatusCountResponse;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response.CollectorWorkHistoryItemResponse;

import java.util.List;

public interface CollectorService {

    List<CollectorTaskResponse> getTasks(Integer collectorId, String status, boolean all);

    List<CollectorTaskStatusCountResponse> getTaskStatusCounts(Integer collectorId);

    List<CollectorWorkHistoryItemResponse> getWorkHistory(Integer collectorId, String status);

    CollectorPerformanceStatsResponse getStats(Integer collectorId, Integer year);

    void acceptTask(Integer requestId, Integer collectorId);

    void startTask(Integer requestId, Integer collectorId);

    void rejectTask(Integer requestId, Integer collectorId, String reason);

    void updateStatus(Integer requestId, Integer collectorId, String status);

    void completeTask(Integer requestId, Integer collectorId);

    void updateAvailabilityStatus(Integer collectorId, String status);
}
