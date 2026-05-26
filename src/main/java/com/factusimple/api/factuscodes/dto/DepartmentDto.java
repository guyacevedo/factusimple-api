package com.factusimple.api.factuscodes.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class DepartmentDto {
    private String code;
    private String name;
}
