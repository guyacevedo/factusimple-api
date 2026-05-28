package com.factusimple.api.plan.mapper;

import com.factusimple.api.plan.dto.PlanRequestDto;
import com.factusimple.api.plan.dto.PlanResponseDto;
import com.factusimple.api.plan.entity.Plan;
import com.factusimple.api.shared.mapper.BaseMapper;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface PlanMapper extends BaseMapper<Plan, PlanResponseDto> {

    PlanResponseDto toDto(Plan plan);

    Plan toEntity(PlanRequestDto dto);

    void updateEntity(PlanRequestDto dto, @MappingTarget Plan plan);
}
