package com.factusimple.api.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseDto {

    private UUID id;
    private String email;
    private String firstName;
    private String lastName;
    private String phone;
    private String role;
    private String planName;
    private Integer maxProducts;
    private Integer productCount;
    private Integer maxCustomers;
    private Integer customerCount;
    private Integer maxInvoices;
    private Integer invoiceCount;
    private Boolean isActive;
}