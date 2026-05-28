package com.factusimple.api.establishment.mapper;

import com.factusimple.api.establishment.dto.EstablishmentRequestDto;
import com.factusimple.api.establishment.dto.EstablishmentResponseDto;
import com.factusimple.api.establishment.entity.Establishment;
import com.factusimple.api.shared.mapper.BaseMapper;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface EstablishmentMapper extends BaseMapper<Establishment, EstablishmentResponseDto> {

    @Mapping(source = "user.id", target = "userId")
    EstablishmentResponseDto toDto(Establishment establishment);

    @Mapping(target = "user", ignore = true)
    Establishment toEntity(EstablishmentRequestDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "user", ignore = true)
    void updateEntity(EstablishmentRequestDto dto, @MappingTarget Establishment establishment);
}
