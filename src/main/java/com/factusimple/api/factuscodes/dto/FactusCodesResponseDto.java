package com.factusimple.api.factuscodes.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class FactusCodesResponseDto {
    private List<FactusCodeItemDto> taxes;
    private List<FactusCodeItemDto> withholdingTaxes;
    private List<FactusCodeItemDto> tributeCodes;
    private List<FactusCodeItemDto> paymentForms;
    private List<FactusCodeItemDto> paymentMethods;
    private List<FactusCodeItemDto> allowanceChargeConcepts;
    private List<FactusCodeItemDto> identityDocumentTypes;
    private List<FactusCodeItemDto> legalOrgTypes;
    private List<FactusCodeItemDto> invoiceOperationTypes;
    private List<FactusCodeItemDto> fiscalResponsibilities;
    private List<FactusCodeItemDto> productStandards;
    private List<FactusCodeItemDto> invoiceCorrections;
    private List<FactusCodeItemDto> creditNoteOperationTypes;
    private List<UnitMeasureDto> unitMeasures;
}
