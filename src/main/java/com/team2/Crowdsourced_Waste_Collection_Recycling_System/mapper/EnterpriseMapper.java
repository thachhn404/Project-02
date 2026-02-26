package com.team2.Crowdsourced_Waste_Collection_Recycling_System.mapper;

import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response.EnterpriseResponse;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.Enterprise;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface EnterpriseMapper {
    EnterpriseResponse toResponse(Enterprise enterprise);
    List<EnterpriseResponse> toResponses(List<Enterprise> enterprises);
}

