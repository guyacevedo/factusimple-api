package com.factusimple.api.user.mapper;


import com.factusimple.api.shared.mapper.BaseMapper;
import com.factusimple.api.user.dto.UserResponseDto;
import com.factusimple.api.user.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper extends BaseMapper<User, UserResponseDto> {

    @Mapping(source = "role", target = "role")
    @Mapping(source = "plan.name", target = "planName")
    @Mapping(source = "plan.maxProducts", target = "maxProducts")
    @Mapping(source = "plan.maxCustomers", target = "maxCustomers")
    @Mapping(source = "plan.maxInvoices", target = "maxInvoices")
    UserResponseDto toDto(User user);

    @Mapping(target = "role", ignore = true)
    @Mapping(target = "plan", ignore = true)
    @Mapping(target = "password", ignore = true)
    User toEntity(UserResponseDto dto);
}