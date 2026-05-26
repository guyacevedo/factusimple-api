package com.factusimple.api.establishment.service;

import com.factusimple.api.establishment.dto.EstablishmentRequestDto;
import com.factusimple.api.establishment.dto.EstablishmentResponseDto;
import com.factusimple.api.establishment.entity.Establishment;
import com.factusimple.api.establishment.mapper.EstablishmentMapper;
import com.factusimple.api.establishment.repository.EstablishmentRepository;
import com.factusimple.api.infrastructure.exception.*;
import com.factusimple.api.infrastructure.factus.codes.NumberingRangeIdCode;
import com.factusimple.api.user.entity.User;
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
public class EstablishmentService {

    private final EstablishmentRepository establishmentRepository;
    private final EstablishmentMapper establishmentMapper;

    /**
     * Crea un Establishment para el User dado. Usar desde AuthService.register
     * para garantizar que cada ESTABLISHMENT tenga su establecimiento en la misma transacción.
     */
    @Transactional
    public void createForUser(User user, EstablishmentRequestDto requestDto) {
        if (establishmentRepository.existsByUserId(user.getId())) {
            throw new ConflictException("El usuario ya tiene un establecimiento asociado");
        }
        if (establishmentRepository.existsByNit(requestDto.getNit())) {
            throw new ConflictException("Ya existe un establecimiento con NIT " + requestDto.getNit());
        }

        Establishment establishment = establishmentMapper.toEntity(requestDto);
        establishment.setUser(user);
        establishment.setNumberingRangeId(Integer.parseInt(NumberingRangeIdCode.FASI.getCode()));
        Establishment saved = establishmentRepository.save(establishment);
        log.info("Establishment creado: id={}, nit={}, userId={}",
                saved.getId(), saved.getNit(), user.getId());
    }

    @Transactional(readOnly = true)
    public EstablishmentResponseDto getMine(UUID userId) {
        return establishmentMapper.toDto(getEntityByUserId(userId));
    }

    /**
     * Resolución del Establishment del usuario autenticado, usado por otros módulos
     * (products, customers, invoices) para garantizar el scope por establecimiento.
     */
    @Transactional(readOnly = true)
    public Establishment getEntityByUserId(UUID userId) {
        return establishmentRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Establishment", "userId", userId));
    }

    @Transactional
    public EstablishmentResponseDto update(UUID userId, EstablishmentRequestDto requestDto) {
        Establishment establishment = getEntityByUserId(userId);

        if (!establishment.getNit().equals(requestDto.getNit())
                && establishmentRepository.existsByNit(requestDto.getNit())) {
            throw new ConflictException("Ya existe un establecimiento con NIT " + requestDto.getNit());
        }

        establishmentMapper.updateEntity(requestDto, establishment);
        Establishment saved = establishmentRepository.save(establishment);
        log.info("Establishment actualizado: id={}", saved.getId());
        return establishmentMapper.toDto(saved);
    }

    @Transactional(readOnly = true)
    public Page<EstablishmentResponseDto> listEstablishments(Pageable pageable) {
        log.debug("List establishment esta with pagination: page={}, size={}", pageable.getPageNumber(), pageable.getPageSize());

        return establishmentRepository.findAll(pageable)
                .map(establishmentMapper::toDto);
    }
}
