package com.factusimple.api.customer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerResponseDto {

    private UUID id;
    private UUID establishmentId;
    private String idTypeCode;
    private String identification;
    private String dv;
    private String legalOrgCode;
    private String company;
    private String names;
    private String tradeName;
    private String address;
    private String email;
    private String phone;
    private String municipalityCode;
    private String tributeCode;
    private String fiscalResponsibility;
    private BigDecimal creditLimit;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
