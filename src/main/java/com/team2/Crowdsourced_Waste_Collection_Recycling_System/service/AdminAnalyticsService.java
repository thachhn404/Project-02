package com.team2.Crowdsourced_Waste_Collection_Recycling_System.service;

import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response.AdminCollectedWeightChartResponse;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response.AdminCollectedWeightDailyChartResponse;

import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response.AdminSystemAnalyticsResponse;

public interface AdminAnalyticsService {
    AdminSystemAnalyticsResponse getSystemAnalytics();

    AdminCollectedWeightChartResponse getCollectedWeightChart(Integer year);

    AdminCollectedWeightDailyChartResponse getCollectedWeightDailyChart(Integer year, Integer month);
}
