package com.factusimple.api.customer.mapper;

import com.factusimple.api.customer.dto.CustomerRequestDto;
import com.factusimple.api.customer.dto.CustomerResponseDto;
import com.factusimple.api.customer.entity.Customer;
import com.factusimple.api.shared.mapper.BaseMapper;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface CustomerMapper extends BaseMapper<Customer, CustomerResponseDto> {

    @Mapping(source = "establishment.id", target = "establishmentId")
    CustomerResponseDto toDto(Customer customer);

    @Mapping(target = "establishment", ignore = true)
    Customer toEntity(CustomerRequestDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "establishment", ignore = true)
    void updateEntity(CustomerRequestDto dto, @MappingTarget Customer customer);
}
