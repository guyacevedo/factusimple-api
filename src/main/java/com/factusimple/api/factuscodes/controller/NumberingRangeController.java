package com.factusimple.api.factuscodes.controller;

import com.factusimple.api.factuscodes.dto.NumberingRangeDto;
import com.factusimple.api.factuscodes.service.NumberingRangeService;
import com.factusimple.api.infrastructure.filter.CustomUserDetails;
import com.factusimple.api.shared.dto.ApiResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/numbering-ranges")
@RequiredArgsConstructor
public class NumberingRangeController {

    private final NumberingRangeService numberingRangeService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponseDto<NumberingRangeDto>> getActive(
            @AuthenticationPrincipal CustomUserDetails principal) {
        NumberingRangeDto dto = numberingRangeService.getActive(principal.getUserId());
        return ResponseEntity.ok(ApiResponseDto.success("Rango de numeración obtenido exitosamente", dto));
    }
}
