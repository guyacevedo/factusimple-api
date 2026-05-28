package com.factusimple.api.factuscodes.controller;

import com.factusimple.api.factuscodes.dto.FactusCodesResponseDto;
import com.factusimple.api.factuscodes.service.FactusCodesService;
import com.factusimple.api.shared.dto.ApiResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/factus-codes")
@RequiredArgsConstructor
public class FactusCodesController {

    private final FactusCodesService factusCodesService;

    @GetMapping
    public ApiResponseDto<FactusCodesResponseDto> getAll() {
        return ApiResponseDto.success("OK", factusCodesService.getAll());
    }
}
