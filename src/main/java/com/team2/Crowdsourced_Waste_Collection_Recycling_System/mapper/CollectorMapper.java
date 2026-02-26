package com.team2.Crowdsourced_Waste_Collection_Recycling_System.mapper;

import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response.CreateCollectorResponse;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.Collector;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CollectorMapper {

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "collectorId", source = "id")
    @Mapping(target = "enterpriseId", source = "enterprise.id")
    @Mapping(target = "email", source = "email")
    @Mapping(target = "fullName", source = "fullName")
    @Mapping(target = "phone", expression = "java(collector.getUser() != null ? collector.getUser().getPhone() : null)")
    @Mapping(target = "employeeCode", source = "employeeCode")
    @Mapping(target = "status", expression = "java(collector.getStatus() != null ? collector.getStatus().name().toLowerCase() : null)")
    @Mapping(target = "vehicleType", source = "vehicleType")
    @Mapping(target = "vehiclePlate", source = "vehiclePlate")
    @Mapping(target = "violationCount", source = "violationCount")
    CreateCollectorResponse toCreateCollectorResponse(Collector collector);

    List<CreateCollectorResponse> toCreateCollectorResponses(List<Collector> collectors);
}

