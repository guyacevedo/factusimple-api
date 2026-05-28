package com.factusimple.api.factuscodes.dto;

import java.util.List;

public record FactusCodesResponseDto(
    List<FactusCodeItemDto> taxes,
    List<FactusCodeItemDto> withholdingTaxes,
    List<FactusCodeItemDto> tributeCodes,
    List<FactusCodeItemDto> paymentForms,
    List<FactusCodeItemDto> paymentMethods,
    List<FactusCodeItemDto> allowanceChargeConcepts,
    List<FactusCodeItemDto> identityDocumentTypes,
    List<FactusCodeItemDto> legalOrgTypes,
    List<FactusCodeItemDto> invoiceOperationTypes,
    List<FactusCodeItemDto> fiscalResponsibilities,
    List<FactusCodeItemDto> productStandards,
    List<FactusCodeItemDto> invoiceCorrections,
    List<FactusCodeItemDto> creditNoteOperationTypes,
    List<UnitMeasureDto> unitMeasures,
    List<MunicipalityDto> municipalities
) {}
