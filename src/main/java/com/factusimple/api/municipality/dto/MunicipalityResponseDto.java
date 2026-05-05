package com.factusimple.api.municipality.dto;

import lombok.Data;

import java.util.List;

@Data
public class MunicipalityResponseDto {
    private List<MunicipalityDto> municipalities;
}