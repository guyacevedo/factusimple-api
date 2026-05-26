package com.factusimple.api.factuscodes.dto;

import lombok.Data;

import java.util.List;

@Data
public class MunicipalityResponseDto {
    private List<MunicipalityDto> municipalities;
}