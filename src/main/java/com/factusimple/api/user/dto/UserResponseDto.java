package com.factusimple.api.user.dto;

import java.util.UUID;

public record UserResponseDto(
    UUID id,
    String email,
    String firstName,
    String lastName,
    String phone,
    String role,
    String planId,
    String planName,
    Integer maxProducts,
    Integer productCount,
    Integer maxCustomers,
    Integer customerCount,
    Integer maxInvoices,
    Integer invoiceCount,
    Boolean isActive
) {}