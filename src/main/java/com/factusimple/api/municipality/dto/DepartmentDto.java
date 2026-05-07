package com.factusimple.api.municipality.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class DepartmentDto {
    private String code;
    private String name;
}
