package com.factusimple.api.customer.service;

import com.factusimple.api.customer.dto.CustomerRequestDto;
import com.factusimple.api.customer.dto.CustomerResponseDto;
import com.factusimple.api.customer.entity.Customer;
import com.factusimple.api.customer.mapper.CustomerMapper;
import com.factusimple.api.customer.repository.CustomerRepository;
import com.factusimple.api.establishment.entity.Establishment;
import com.factusimple.api.establishment.service.EstablishmentService;
import com.factusimple.api.infrastructure.exception.*;
import com.factusimple.api.user.entity.User;
import com.factusimple.api.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerService {

    private static final String LEGAL_ORG_PJ = "1";
    private static final String LEGAL_ORG_PN = "2";

    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;
    private final EstablishmentService establishmentService;
    private final UserRepository userRepository;
    private final EntityManager entityManager;

    @Transactional
    public CustomerResponseDto create(UUID userId, CustomerRequestDto requestDto) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        validateLegalOrgFields(requestDto);
        Establishment establishment = establishmentService.getEntityByUserId(userId);

        if (customerRepository.existsByIdentificationAndEstablishmentId(
                requestDto.identification(), establishment.getId())) {
            throw new ConflictException(
                    "Ya existe un cliente con identificación '"
                            + requestDto.identification() + "' en este establecimiento");
        }

        Customer customer = customerMapper.toEntity(requestDto);
        customer.setEstablishment(establishment);
        if (customer.getIsActive() == null) {
            customer.setIsActive(true);
        }

        Customer saved = customerRepository.save(customer);
        entityManager.flush();

        // Atomic increment: if limit reached, returns 0 (no update)
        int updated = userRepository.incrementCustomersCountIfBelowLimit(userId, user.getPlan().getMaxCustomers());
        if (updated == 0) {
            throw new ForbiddenException(
                    "Límite del plan alcanzado: " + user.getPlan().getMaxCustomers() + " clientes.");
        }

        log.info("Cliente creado: id={}, identification={}, establishmentId={}",
                saved.getId(), saved.getIdentification(), establishment.getId());

        return customerMapper.toDto(saved);
    }

    @Transactional(readOnly = true)
    public Page<CustomerResponseDto> list(UUID userId, Pageable pageable) {
        Establishment establishment = establishmentService.getEntityByUserId(userId);
        return customerRepository.findByEstablishmentId(establishment.getId(), pageable)
                .map(customerMapper::toDto);
    }

    @Transactional(readOnly = true)
    public CustomerResponseDto get(UUID userId, UUID customerId) {
        return customerMapper.toDto(requireOwned(userId, customerId));
    }

    @Transactional
    public CustomerResponseDto update(UUID userId, UUID customerId, CustomerRequestDto requestDto) {
        validateLegalOrgFields(requestDto);
        Customer customer = requireOwned(userId, customerId);

        if (!customer.getIdentification().equals(requestDto.identification())
                && customerRepository.existsByIdentificationAndEstablishmentId(
                        requestDto.identification(), customer.getEstablishment().getId())) {
            throw new ConflictException(
                    "Ya existe un cliente con identificación '"
                            + requestDto.identification() + "' en este establecimiento");
        }

        customerMapper.updateEntity(requestDto, customer);
        Customer saved = customerRepository.save(customer);
        log.info("Cliente actualizado: id={}", saved.getId());
        return customerMapper.toDto(saved);
    }

    @Transactional
    public void delete(UUID userId, UUID customerId) {
        Customer customer = requireOwned(userId, customerId);
        customerRepository.delete(customer);
        userRepository.decrementCustomersCount(userId);
        log.info("Cliente eliminado: id={}", customerId);
    }

    private Customer requireOwned(UUID userId, UUID customerId) {
        Establishment establishment = establishmentService.getEntityByUserId(userId);
        return customerRepository.findByIdAndEstablishmentId(customerId, establishment.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", customerId));
    }

    /**
     * Persona Jurídica (legalOrgCode = "1") requiere razón social (`company`).
     * Persona Natural (legalOrgCode = "2") requiere `names`.
     */
    private void validateLegalOrgFields(CustomerRequestDto dto) {
        String code = dto.legalOrgCode();
        if (code == null) {
            return;
        }
        if (LEGAL_ORG_PJ.equals(code) && (dto.company() == null || dto.company().isBlank())) {
            throw new BadRequestException("Persona Jurídica requiere razón social ('company')");
        }
        if (LEGAL_ORG_PN.equals(code) && (dto.names() == null || dto.names().isBlank())) {
            throw new BadRequestException("Persona Natural requiere nombres ('names')");
        }
    }
}
