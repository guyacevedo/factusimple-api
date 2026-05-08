package com.factusimple.api.plan.service;

import com.factusimple.api.infrastructure.exception.ApiException;
import com.factusimple.api.infrastructure.exception.ResourceNotFoundException;
import com.factusimple.api.plan.dto.PlanRequestDto;
import com.factusimple.api.plan.dto.PlanResponseDto;
import com.factusimple.api.plan.entity.Plan;
import com.factusimple.api.plan.mapper.PlanMapper;
import com.factusimple.api.plan.repository.PlanRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PlanService {

    private final PlanRepository planRepository;
    private final PlanMapper planMapper;

    @Transactional
    public PlanResponseDto createPlan(PlanRequestDto requestDto) {
        log.info("Creating plan: {}", requestDto.getName());

        // Check if plan name already exists
        if (planRepository.findByName(requestDto.getName()).isPresent()) {
            throw new ApiException(409, "Plan with name '" + requestDto.getName() + "' already exists");
        }

        Plan plan = planMapper.toEntity(requestDto);
        plan = planRepository.save(plan);

        log.info("Plan created successfully: id={}, name={}", plan.getId(), plan.getName());
        return planMapper.toDto(plan);
    }

    @Transactional(readOnly = true)
    public PlanResponseDto getPlan(UUID id) {
        log.debug("Fetching plan: id={}", id);

        Plan plan = planRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Plan not found with id: " + id));

        return planMapper.toDto(plan);
    }

    @Transactional(readOnly = true)
    public Page<PlanResponseDto> listPlans(Pageable pageable) {
        log.debug("Listing plans with pagination: page={}, size={}", pageable.getPageNumber(), pageable.getPageSize());

        return planRepository.findAll(pageable)
                .map(planMapper::toDto);
    }

    @Transactional
    public PlanResponseDto updatePlan(UUID id, PlanRequestDto requestDto) {
        log.info("Updating plan: id={}", id);

        Plan plan = planRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Plan not found with id: " + id));

        // Check if new name conflicts with another plan (if name is being changed)
        if (!plan.getName().equals(requestDto.getName()) &&
            planRepository.findByName(requestDto.getName()).isPresent()) {
            throw new ApiException(409, "Plan with name '" + requestDto.getName() + "' already exists");
        }

        planMapper.updateEntity(requestDto, plan);
        plan = planRepository.save(plan);

        log.info("Plan updated successfully: id={}", id);
        return planMapper.toDto(plan);
    }

    @Transactional
    public void deletePlan(UUID id) {
        log.info("Deleting plan: id={}", id);

        Plan plan = planRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Plan not found with id: " + id));

        planRepository.delete(plan);
        log.info("Plan deleted successfully: id={}", id);
    }
}
