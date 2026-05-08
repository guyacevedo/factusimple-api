package com.factusimple.api.factuscodes.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FactusCodeItemDto {
    private String code;
    private String name;
}
