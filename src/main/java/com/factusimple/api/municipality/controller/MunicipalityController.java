package com.factusimple.api.municipality.controller;

import com.factusimple.api.municipality.dto.MunicipalityDto;
import com.factusimple.api.municipality.service.MunicipalityService;
import com.factusimple.api.shared.dto.ApiResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/v1")
@RequiredArgsConstructor
public class MunicipalityController {

    private final MunicipalityService municipalityService;

    @GetMapping("/municipalities")
    public ApiResponseDto<List<MunicipalityDto>> getAll(
            @RequestParam(required = false) String departmentCode) {

        List<MunicipalityDto> result = municipalityService.getAll();

        if (departmentCode != null) {
            result = result.stream()
                    .filter(m -> m.getDepartment().getCode().equals(departmentCode))
                    .toList();
        }

        return ApiResponseDto.success("OK", result);
    }
}
