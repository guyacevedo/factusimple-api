package com.factusimple.api.municipality.dto;

import lombok.Data;

@Data
public class MunicipalityDto {
    private String code;
    private String name;
    private DepartmentDto department;
}