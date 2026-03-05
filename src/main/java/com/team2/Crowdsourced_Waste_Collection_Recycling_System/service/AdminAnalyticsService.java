package com.team2.Crowdsourced_Waste_Collection_Recycling_System.service;

import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response.AdminCollectedWeightChartResponse;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response.AdminCollectedWeightDailyChartResponse;

public interface AdminAnalyticsService {
    AdminCollectedWeightChartResponse getCollectedWeightChart(Integer year);

    AdminCollectedWeightDailyChartResponse getCollectedWeightDailyChart(Integer year, Integer month);
}
