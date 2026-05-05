package com.factusimple.api.customer.service;

import com.factusimple.api.customer.dto.CustomerRequestDto;
import com.factusimple.api.customer.dto.CustomerResponseDto;
import com.factusimple.api.customer.entity.Customer;
import com.factusimple.api.customer.mapper.CustomerMapper;
import com.factusimple.api.customer.repository.CustomerRepository;
import com.factusimple.api.establishments.entity.Establishment;
import com.factusimple.api.establishments.service.EstablishmentService;
import com.factusimple.api.infrastructure.exception.ApiException;
import com.factusimple.api.infrastructure.exception.ResourceNotFoundException;
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
public class CustomerService {

    private static final String LEGAL_ORG_PJ = "1";
    private static final String LEGAL_ORG_PN = "2";

    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;
    private final EstablishmentService establishmentService;

    @Transactional
    public CustomerResponseDto create(UUID userId, CustomerRequestDto requestDto) {
        validateLegalOrgFields(requestDto);
        Establishment establishment = establishmentService.getEntityByUserId(userId);

        if (customerRepository.existsByIdentificationAndEstablishmentId(
                requestDto.getIdentification(), establishment.getId())) {
            throw new ApiException(409,
                    "Ya existe un cliente con identificación '"
                            + requestDto.getIdentification() + "' en este establecimiento");
        }

        Customer customer = customerMapper.toEntity(requestDto);
        customer.setEstablishment(establishment);
        if (customer.getIsActive() == null) {
            customer.setIsActive(true);
        }

        Customer saved = customerRepository.save(customer);
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

        if (!customer.getIdentification().equals(requestDto.getIdentification())
                && customerRepository.existsByIdentificationAndEstablishmentId(
                        requestDto.getIdentification(), customer.getEstablishment().getId())) {
            throw new ApiException(409,
                    "Ya existe un cliente con identificación '"
                            + requestDto.getIdentification() + "' en este establecimiento");
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
        String code = dto.getLegalOrgCode();
        if (code == null) {
            return;
        }
        if (LEGAL_ORG_PJ.equals(code) && (dto.getCompany() == null || dto.getCompany().isBlank())) {
            throw new ApiException(400, "Persona Jurídica requiere razón social ('company')");
        }
        if (LEGAL_ORG_PN.equals(code) && (dto.getNames() == null || dto.getNames().isBlank())) {
            throw new ApiException(400, "Persona Natural requiere nombres ('names')");
        }
    }
}
