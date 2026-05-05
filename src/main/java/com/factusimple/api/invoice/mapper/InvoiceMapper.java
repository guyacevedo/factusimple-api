package com.factusimple.api.invoice.mapper;

import com.factusimple.api.invoice.dto.*;
import com.factusimple.api.invoice.entity.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface InvoiceMapper {

    // ----- Invoice -----

    @Mapping(source = "establishment.id", target = "establishmentId")
    @Mapping(source = "customer.id", target = "customerId")
    InvoiceResponseDto toDto(Invoice invoice);

    /**
     * Construye un Invoice "desnudo" desde el DTO. El service asigna después:
     * establishment, customer, status, totales, hijos y back-references.
     */
    @Mapping(target = "establishment", ignore = true)
    @Mapping(target = "customer", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "subtotal", ignore = true)
    @Mapping(target = "totalTaxes", ignore = true)
    @Mapping(target = "totalDiscounts", ignore = true)
    @Mapping(target = "total", ignore = true)
    @Mapping(target = "cufe", ignore = true)
    @Mapping(target = "xmlUrl", ignore = true)
    @Mapping(target = "factusNumber", ignore = true)
    @Mapping(target = "items", ignore = true)
    @Mapping(target = "payments", ignore = true)
    @Mapping(target = "prepayments", ignore = true)
    @Mapping(target = "allowanceCharges", ignore = true)
    Invoice toEntity(InvoiceRequestDto dto);

    // ----- Items -----

    @Mapping(source = "product.id", target = "productId")
    InvoiceItemResponseDto itemToDto(InvoiceItem item);

    List<InvoiceItemResponseDto> itemsToDto(List<InvoiceItem> items);

    @Mapping(target = "invoice", ignore = true)
    @Mapping(target = "product", ignore = true)
    InvoiceItem itemToEntity(InvoiceItemRequestDto dto);

    List<InvoiceItem> itemsToEntity(List<InvoiceItemRequestDto> dtos);

    // ----- Item Taxes -----

    InvoiceItemTaxResponseDto taxToDto(InvoiceItemTax tax);

    List<InvoiceItemTaxResponseDto> taxesToDto(List<InvoiceItemTax> taxes);

    @Mapping(target = "item", ignore = true)
    InvoiceItemTax taxToEntity(InvoiceItemTaxRequestDto dto);

    List<InvoiceItemTax> taxesToEntity(List<InvoiceItemTaxRequestDto> dtos);

    // ----- Payments -----

    InvoicePaymentResponseDto paymentToDto(InvoicePayment payment);

    List<InvoicePaymentResponseDto> paymentsToDto(List<InvoicePayment> payments);

    @Mapping(target = "invoice", ignore = true)
    InvoicePayment paymentToEntity(InvoicePaymentRequestDto dto);

    List<InvoicePayment> paymentsToEntity(List<InvoicePaymentRequestDto> dtos);

    // ----- Prepayments -----

    InvoicePrepaymentResponseDto prepaymentToDto(InvoicePrepayment prepayment);

    List<InvoicePrepaymentResponseDto> prepaymentsToDto(List<InvoicePrepayment> prepayments);

    @Mapping(target = "invoice", ignore = true)
    InvoicePrepayment prepaymentToEntity(InvoicePrepaymentRequestDto dto);

    List<InvoicePrepayment> prepaymentsToEntity(List<InvoicePrepaymentRequestDto> dtos);

    // ----- Allowance/Charges -----

    AllowanceChargeResponseDto allowanceChargeToDto(AllowanceCharge ac);

    List<AllowanceChargeResponseDto> allowanceChargesToDto(List<AllowanceCharge> acs);

    @Mapping(target = "invoice", ignore = true)
    AllowanceCharge allowanceChargeToEntity(AllowanceChargeRequestDto dto);

    List<AllowanceCharge> allowanceChargesToEntity(List<AllowanceChargeRequestDto> dtos);
}
