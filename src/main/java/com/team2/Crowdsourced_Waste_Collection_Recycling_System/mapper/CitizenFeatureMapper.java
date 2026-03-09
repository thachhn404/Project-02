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

    @Mapping(target = "reportId", expression = "java(transaction.getReport() != null ? transaction.getReport().getId() : (transaction.getCollectionRequest() != null && transaction.getCollectionRequest().getReport() != null ? transaction.getCollectionRequest().getReport().getId() : null))")
    @Mapping(target = "collectionId", expression = "java(transaction.getCollectionRequest() != null ? transaction.getCollectionRequest().getId() : null)")
    @Mapping(target = "reportCode", expression = "java(transaction.getReport() != null ? transaction.getReport().getReportCode() : (transaction.getCollectionRequest() != null && transaction.getCollectionRequest().getReport() != null ? transaction.getCollectionRequest().getReport().getReportCode() : null))")
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
    @Mapping(target = "status", constant = "PENDING")
    @Mapping(target = "subject", expression = "java(request.getReportId() != null ? \"Complaint for Report #\" + request.getReportId() + \" - \" + request.getType() : \"General Complaint - \" + request.getType())")
    @Mapping(target = "content", source = "content")
    @Mapping(target = "feedbackType", source = "type")
    @Mapping(target = "rating", source = "rating")
    @Mapping(target = "resolution", ignore = true)
    Feedback toFeedback(CreateComplaintRequest request);
}
