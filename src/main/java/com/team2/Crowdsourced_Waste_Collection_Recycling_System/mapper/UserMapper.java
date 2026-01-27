package com.team2.Crowdsourced_Waste_Collection_Recycling_System.mapper;

import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.UserDto;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.User;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(source = "role.roleCode", target = "roleCode")
    UserDto toDto(User user);

    @InheritInverseConfiguration(name = "toDto")
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "phone", ignore = true)
    @Mapping(target = "lastLogin", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    User toEntity(UserDto dto);
}
