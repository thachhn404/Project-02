package com.team2.Crowdsourced_Waste_Collection_Recycling_System.service;

import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.request.EnterpriseFeedbackResolveRequest;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response.EnterpriseFeedbackResponse;

import java.util.List;

public interface AdminComplaintService {
    List<EnterpriseFeedbackResponse> getAllComplaints();
    EnterpriseFeedbackResponse getComplaintDetail(Integer id);
    void resolveComplaint(Integer id, EnterpriseFeedbackResolveRequest request);
}
