package com.team2.Crowdsourced_Waste_Collection_Recycling_System.mapper;

import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.request.CreateComplaintRequest;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response.CitizenLeaderboardResponse;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response.CitizenRewardHistoryResponse;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response.ComplaintResponse;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.Citizen;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.Feedback;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.PointTransaction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CitizenFeatureMapper {

    @Mapping(target = "reportId", source = "collectionRequest.id")
    @Mapping(target = "reportCode", source = "collectionRequest.report.reportCode")
    @Mapping(target = "point", source = "points")
    CitizenRewardHistoryResponse toCitizenRewardHistoryResponse(PointTransaction transaction);

    @Mapping(target = "citizenId", source = "id")
    @Mapping(target = "totalPoint", source = "totalPoints", defaultValue = "0")
    @Mapping(target = "rank", ignore = true) // Rank is calculated externally
    CitizenLeaderboardResponse toCitizenLeaderboardResponse(Citizen citizen);

    @Mapping(target = "reportId", expression = "java(feedback.getCollectionRequest() != null && feedback.getCollectionRequest().getReport() != null ? feedback.getCollectionRequest().getReport().getId() : null)")
    @Mapping(target = "type", source = "feedbackType")
    @Mapping(target = "content", source = "content")
    ComplaintResponse toComplaintResponse(Feedback feedback);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "citizen", ignore = true)
    @Mapping(target = "collectionRequest", ignore = true)
    @Mapping(target = "feedbackCode", ignore = true)
    @Mapping(target = "createdAt", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "updatedAt", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "status", constant = "OPEN")
    @Mapping(target = "severity", constant = "MEDIUM")
    @Mapping(target = "subject", expression = "java(\"Complaint for Report #\" + request.getReportId() + \" - \" + request.getType())")
    @Mapping(target = "content", source = "content")
    @Mapping(target = "feedbackType", source = "type")
    Feedback toFeedback(CreateComplaintRequest request);
}
