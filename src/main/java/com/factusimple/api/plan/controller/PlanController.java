package com.factusimple.api.plan.controller;

import com.factusimple.api.plan.dto.PlanRequestDto;
import com.factusimple.api.plan.dto.PlanResponseDto;
import com.factusimple.api.plan.service.PlanService;
import com.factusimple.api.shared.dto.ApiResponseDto;
import com.factusimple.api.shared.dto.PageResponseDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/v1/plans")
@RequiredArgsConstructor
public class PlanController {

    private final PlanService planService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDto<PlanResponseDto>> createPlan(
            @Valid @RequestBody PlanRequestDto requestDto) {

        PlanResponseDto planDto = planService.createPlan(requestDto);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDto.success("Plan created successfully", planDto ));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponseDto<PlanResponseDto>> getPlan(@PathVariable UUID id) {
        
        PlanResponseDto planDto = planService.getPlan(id);
        
        return ResponseEntity.ok(ApiResponseDto.success("Plan retrieved successfully", planDto));
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponseDto<PageResponseDto<PlanResponseDto>>> listPlans(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<PlanResponseDto> plansPage = planService.listPlans(pageable);
        return ResponseEntity.ok(ApiResponseDto.success("Plans retrieved successfully", PageResponseDto.from(plansPage)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDto<PlanResponseDto>> updatePlan(
            @PathVariable UUID id,
            @Valid @RequestBody PlanRequestDto requestDto) {
        
        PlanResponseDto planDto = planService.updatePlan(id, requestDto);

        return ResponseEntity.ok(ApiResponseDto.success("Plan updated successfully", planDto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDto<Void>> deletePlan(@PathVariable UUID id) {
        
        planService.deletePlan(id);

        return ResponseEntity.ok(ApiResponseDto.success("Plan deleted successfully",null));
    }
}
