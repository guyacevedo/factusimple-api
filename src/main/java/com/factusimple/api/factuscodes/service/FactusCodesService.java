package com.factusimple.api.factuscodes.service;

import com.factusimple.api.factuscodes.dto.*;
import com.factusimple.api.infrastructure.factus.codes.AllowanceChargeConceptCode;
import com.factusimple.api.infrastructure.factus.codes.CreditNoteOperationType;
import com.factusimple.api.infrastructure.factus.codes.FactusCode;
import com.factusimple.api.infrastructure.factus.codes.FiscalResponsibilityCode;
import com.factusimple.api.infrastructure.factus.codes.IdentityDocumentType;
import com.factusimple.api.infrastructure.factus.codes.InvoiceCorrection;
import com.factusimple.api.infrastructure.factus.codes.InvoiceOperationType;
import com.factusimple.api.infrastructure.factus.codes.LegalOrgCode;
import com.factusimple.api.infrastructure.factus.codes.PaymentFormCode;
import com.factusimple.api.infrastructure.factus.codes.PaymentMethodCode;
import com.factusimple.api.infrastructure.factus.codes.ProductStandardCode;
import com.factusimple.api.infrastructure.factus.codes.TaxCode;
import com.factusimple.api.infrastructure.factus.codes.TributeCode;
import com.factusimple.api.infrastructure.factus.codes.WithholdingTaxCode;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class FactusCodesService {

    private final ObjectMapper objectMapper;
    private FactusCodesResponseDto cache;

    public FactusCodesService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    public void loadData() {
        try {
            List<UnitMeasureDto> unitMeasures = loadUnitMeasures();
            List<MunicipalityDto>  municipalities = loadMunicipalities();

            cache = new FactusCodesResponseDto(
                    fromEnum(TaxCode.class),
                    fromEnum(WithholdingTaxCode.class),
                    fromEnum(TributeCode.class),
                    fromEnum(PaymentFormCode.class),
                    fromEnum(PaymentMethodCode.class),
                    fromEnum(AllowanceChargeConceptCode.class),
                    fromEnum(IdentityDocumentType.class),
                    fromEnum(LegalOrgCode.class),
                    fromEnum(InvoiceOperationType.class),
                    fromEnum(FiscalResponsibilityCode.class),
                    fromEnum(ProductStandardCode.class),
                    fromEnum(InvoiceCorrection.class),
                    fromEnum(CreditNoteOperationType.class),
                    unitMeasures,
                    municipalities
            );

            log.info("Factus codes loaded successfully into cache");
        } catch (IOException e) {
            log.error("Error loading Factus codes data", e);
            throw new RuntimeException("Failed to load Factus codes", e);
        }
    }

    public FactusCodesResponseDto getAll() {
        return cache;
    }

    private List<UnitMeasureDto> loadUnitMeasures() throws IOException {
        try (InputStream inputStream = getClass().getResourceAsStream("/data/unit_measures.json")) {
            if (inputStream == null) {
                throw new RuntimeException("unit_measures.json file not found");
            }

            UnitMeasureResponseDto response = objectMapper.readValue(inputStream, UnitMeasureResponseDto.class);
            return Collections.unmodifiableList(response.unitMeasures());
        }
    }

    private List<MunicipalityDto> loadMunicipalities() throws IOException {
        try (InputStream inputStream = getClass().getResourceAsStream("/data/municipalities.json")) {
            if (inputStream == null) {
                throw new RuntimeException("municipalities.json file not found");
            }
            MunicipalityResponseDto response = objectMapper.readValue(inputStream, MunicipalityResponseDto.class);
            return Collections.unmodifiableList(response.municipalities());
        }
    }

    private <E extends Enum<E> & FactusCode> List<FactusCodeItemDto> fromEnum(Class<E> enumClass) {
        return Collections.unmodifiableList(
                Arrays.stream(enumClass.getEnumConstants())
                        .map(e -> new FactusCodeItemDto(e.getCode(), toDisplayName(e.name())))
                        .toList()
        );
    }

    private String toDisplayName(String enumName) {
        return Arrays.stream(enumName.split("_"))
                .map(word -> word.charAt(0) + word.substring(1).toLowerCase())
                .collect(Collectors.joining(" "));
    }
}
