package com.factusimple.api.user.service;

import com.factusimple.api.auth.dto.LoginResponseDto;
import com.factusimple.api.auth.dto.RegisterRequestDto;
import com.factusimple.api.establishments.service.EstablishmentService;
import com.factusimple.api.infrastructure.exception.ResourceNotFoundException;
import com.factusimple.api.plan.entity.Plan;
import com.factusimple.api.plan.repository.PlanRepository;
import com.factusimple.api.user.dto.UserResponseDto;
import com.factusimple.api.user.entity.Role;
import com.factusimple.api.user.entity.User;
import com.factusimple.api.user.mapper.UserMapper;
import com.factusimple.api.user.repository.RoleRepository;
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
public class PlanService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PlanRepository planRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final EstablishmentService establishmentService;

    @Transactional
    public LoginResponseDto createUser(RegisterRequestDto request) {
        // Validar que el email no exista
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("El email ya está registrado");
        }

        // Rol forzado: el registro vía API siempre crea ESTABLISHMENT.
        // Los ADMIN solo se siembran desde DataInitializer.
        Role role = roleRepository.findByName("ESTABLISHMENT")
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Role", "name", "ESTABLISHMENT"));

        // Asignar plan por defecto (STARTED) — el admin puede cambiarlo después
        Plan defaultPlan = planRepository.findByName("STARTED")
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Plan", "name", "STARTED"));

        // Crear usuario
        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phone(request.getPhone())
                .role(role)
                .plan(defaultPlan)
                .productsCount(0)
                .customersCount(0)
                .invoiceCount(0)
                .isActive(true)
                .build();

        User savedUser = userRepository.save(user);
        log.info("Usuario registrado: {}", savedUser.getEmail());

        // Crear el establecimiento asociado en la misma transacción.
        // Los ADMIN no se registran por esta vía (ver SecurityConfig + DataInitializer).
        establishmentService.createForUser(savedUser, request.getEstablishment());

        // Generar tokens de Factus


        // Guardar tokens de Factus vinculados al usuario


        return LoginResponseDto.builder()
                .accessToken("access_token")
                .refreshToken("refresh_token")
                .expiresIn(3600L)
                .user(userMapper.toDto(savedUser))
                .build();
    }


    @Transactional(readOnly = true)
    public Page<UserResponseDto> listUsers(Pageable pageable) {
        log.debug("Listing plans with pagination: page={}, size={}", pageable.getPageNumber(), pageable.getPageSize());

        return userRepository.findAll(pageable)
                .map(userMapper::toDto);
    }


}
