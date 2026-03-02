package com.team2.Crowdsourced_Waste_Collection_Recycling_System.service;

import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response.EnterpriseRequestReportDetailResponse;

public interface EnterpriseReportDetailService {
    EnterpriseRequestReportDetailResponse getRequestReportDetail(Integer enterpriseId, Integer requestId);
}
