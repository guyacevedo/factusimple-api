package com.factusimple.api.product.mapper;

import com.factusimple.api.product.dto.ProductRequestDto;
import com.factusimple.api.product.dto.ProductResponseDto;
import com.factusimple.api.product.entity.Product;
import com.factusimple.api.shared.mapper.BaseMapper;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface ProductMapper extends BaseMapper<Product, ProductResponseDto> {

    @Mapping(source = "establishment.id", target = "establishmentId")
    ProductResponseDto toDto(Product product);

    @Mapping(target = "establishment", ignore = true)
    Product toEntity(ProductRequestDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "establishment", ignore = true)
    void updateEntity(ProductRequestDto dto, @MappingTarget Product product);
}
