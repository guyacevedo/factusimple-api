package com.factusimple.api.factuscodes.dto;

public record MunicipalityDto(
    String code,
    String name,
    DepartmentDto department
) {}