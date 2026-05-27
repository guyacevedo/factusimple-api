package com.factusimple.api.creditnote.mapper;

import com.factusimple.api.creditnote.dto.*;
import com.factusimple.api.creditnote.entity.*;
import com.factusimple.api.shared.dto.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CreditNoteMapper {

    @Mapping(target = "invoiceId", source = "invoice.id")
    @Mapping(target = "invoiceReferenceCode", source = "invoice.referenceCode")
    @Mapping(target = "invoiceFactusNumber", source = "invoice.factusNumber")
    @Mapping(target = "establishmentId", source = "establishment.id")
    CreditNoteResponseDto toDto(CreditNote creditNote);

    @Mapping(target = "establishment", ignore = true)
    @Mapping(target = "invoice", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "factusNumber", ignore = true)
    @Mapping(target = "cufe", ignore = true)
    @Mapping(target = "xmlUrl", ignore = true)
    @Mapping(target = "factusError", ignore = true)
    @Mapping(target = "items", ignore = true)
    @Mapping(target = "payments", ignore = true)
    @Mapping(target = "allowanceCharges", ignore = true)
    CreditNote toEntity(CreditNoteRequestDto dto);

    CreditNoteItemResponseDto itemToDto(CreditNoteItem item);

    @Mapping(target = "creditNote", ignore = true)
    @Mapping(target = "taxes", ignore = true)
    CreditNoteItem itemToEntity(CreditNoteItemRequestDto dto);

    List<CreditNoteItemResponseDto> itemsToDtoList(List<CreditNoteItem> items);

    List<CreditNoteItem> itemsToEntityList(List<CreditNoteItemRequestDto> dtos);

    ItemTaxResponseDto itemTaxToDto(CreditNoteItemTax itemTax);

    @Mapping(target = "item", ignore = true)
    CreditNoteItemTax itemTaxToEntity(ItemTaxRequestDto dto);

    List<ItemTaxResponseDto> itemTaxesToDtoList(List<CreditNoteItemTax> taxes);

    List<CreditNoteItemTax> itemTaxesToEntityList(List<ItemTaxRequestDto> dtos);

    PaymentResponseDto paymentToDto(CreditNotePayment payment);

    @Mapping(target = "creditNote", ignore = true)
    CreditNotePayment paymentToEntity(PaymentRequestDto dto);

    List<PaymentResponseDto> paymentsToDtoList(List<CreditNotePayment> payments);

    List<CreditNotePayment> paymentsToEntityList(List<PaymentRequestDto> dtos);

    AllowanceChargeResponseDto allowanceChargeToDto(CreditNoteAllowanceCharge allowanceCharge);

    @Mapping(target = "creditNote", ignore = true)
    CreditNoteAllowanceCharge allowanceChargeToEntity(CreditNoteAllowanceChargeRequestDto dto);

    List<AllowanceChargeResponseDto> allowanceChargesToDtoList(List<CreditNoteAllowanceCharge> allowanceCharges);

    List<CreditNoteAllowanceCharge> allowanceChargesToEntityList(List<CreditNoteAllowanceChargeRequestDto> dtos);
}
