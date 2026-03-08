package com.team2.Crowdsourced_Waste_Collection_Recycling_System.service;

import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.request.EnterpriseFeedbackResolveRequest;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response.EnterpriseFeedbackResponse;

import java.util.List;

public interface EnterpriseFeedbackService {
    List<EnterpriseFeedbackResponse> getFeedbacks(Integer enterpriseId);
    EnterpriseFeedbackResponse getFeedbackDetail(Integer enterpriseId, Integer feedbackId);
    void resolveFeedback(Integer enterpriseId, Integer feedbackId, EnterpriseFeedbackResolveRequest request);
}
