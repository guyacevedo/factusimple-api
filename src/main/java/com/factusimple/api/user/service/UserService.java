package com.factusimple.api.user.service;

import com.factusimple.api.infrastructure.exception.BadRequestException;
import com.factusimple.api.infrastructure.exception.ResourceNotFoundException;
import com.factusimple.api.infrastructure.exception.UnauthorizedException;
import com.factusimple.api.plan.entity.Plan;
import com.factusimple.api.plan.repository.PlanRepository;
import com.factusimple.api.user.dto.ChangePlanRequestDto;
import com.factusimple.api.user.dto.ChangePasswordRequestDto;
import com.factusimple.api.user.dto.UserResponseDto;
import com.factusimple.api.user.entity.User;
import com.factusimple.api.user.mapper.UserMapper;
import com.factusimple.api.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final PlanRepository planRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public Page<UserResponseDto> listUsers(Pageable pageable) {
        log.debug("Listing plans with pagination: page={}, size={}", pageable.getPageNumber(), pageable.getPageSize());

        return userRepository.findAll(pageable)
                .map(userMapper::toDto);
    }

    @Transactional
    public void changePassword(UUID userId, ChangePasswordRequestDto request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId.toString()));

        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            throw new UnauthorizedException("Contraseña actual incorrecta");
        }

        if (passwordEncoder.matches(request.newPassword(), user.getPassword())) {
            throw new BadRequestException("La nueva contraseña no puede ser igual a la actual");
        }

        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
        log.info("Password changed for user: {}", user.getEmail());
    }

    @Transactional
    public void changePlan(UUID userId, ChangePlanRequestDto request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId.toString()));

        if (user.getPlan().getId().equals(request.planId())) {
            throw new BadRequestException("Ya tienes este plan");
        }

        Plan newPlan = planRepository.findById(request.planId())
                .orElseThrow(() -> new ResourceNotFoundException("Plan", "id", request.planId().toString()));

        if (Boolean.TRUE.equals(newPlan.getRequiredInvitedCode())) {
            if (request.inviteCode() == null || request.inviteCode().isBlank()) {
                throw new BadRequestException("Este plan requiere un código de invitación");
            }
            if (!request.inviteCode().equals(newPlan.getCode())) {
                throw new BadRequestException("El código de invitación es incorrecto");
            }
        }

        if (user.getInvoiceCount() >= newPlan.getMaxInvoices()) {
            throw new BadRequestException(
                    String.format("Tus facturas actuales (%d) superan el límite del nuevo plan (%d)",
                            user.getInvoiceCount(), newPlan.getMaxInvoices())
            );
        }

        if (user.getProductsCount() >= newPlan.getMaxProducts()) {
            throw new BadRequestException(
                    String.format("Tus productos actuales (%d) superan el límite del nuevo plan (%d)",
                            user.getProductsCount(), newPlan.getMaxProducts())
            );
        }

        if (user.getCustomersCount() >= newPlan.getMaxCustomers()) {
            throw new BadRequestException(
                    String.format("Tus clientes actuales (%d) superan el límite del nuevo plan (%d)",
                            user.getCustomersCount(), newPlan.getMaxCustomers())
            );
        }

        user.setPlan(newPlan);
        userRepository.save(user);
        log.info("Plan changed for user: {} to plan: {}", user.getEmail(), newPlan.getName());
    }

}
