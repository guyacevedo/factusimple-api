package com.factusimple.api.infrastructure.factus.client;

import com.factusimple.api.creditnote.entity.CreditNote;
import com.factusimple.api.creditnote.entity.CreditNoteAllowanceCharge;
import com.factusimple.api.creditnote.entity.CreditNoteItem;
import com.factusimple.api.creditnote.entity.CreditNotePayment;
import com.factusimple.api.infrastructure.exception.ApiException;
import com.factusimple.api.infrastructure.factus.dto.creditnote.*;
import com.factusimple.api.user.entity.User;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class FactusCreditNotesClient {

    private final FactusHttpExecutor executor;

    @SuppressWarnings("unchecked")
    @CircuitBreaker(name = "factus", fallbackMethod = "factusUnavailableMapFallback")
    public Map<String, Object> createAtFactus(CreditNote creditNote) {
        User user = creditNote.getEstablishment().getUser();
        FactusCreditNotePayloadDto payload = buildPayload(creditNote);
        return executor.executeWithRetry(user, () -> executor.postJson(user, "/v2/credit-notes/validate", payload, Map.class));
    }

    @SuppressWarnings("unchecked")
    @CircuitBreaker(name = "factus", fallbackMethod = "factusUnavailableResultFallback")
    public FactusCreditNoteResult getDetailsFromFactus(User user, String number) {
        Map<String, Object> response = executor.executeWithRetry(user, () -> executor.getJson(user, "/v2/credit-notes/{number}", Map.class, number));
        return parseResponse(response);
    }

    @CircuitBreaker(name = "factus", fallbackMethod = "factusUnavailableVoidFallback")
    public void deleteAtFactus(User user, String referenceCode) {
        executor.executeWithRetry(user, () -> {
            executor.deleteJson(user, "/v2/credit-notes/reference/{referenceCode}", referenceCode);
            return null;
        });
    }

    @CircuitBreaker(name = "factus", fallbackMethod = "factusUnavailableByteArrayFallback")
    public byte[] downloadPdf(User user, String number) {
        return executor.executeWithRetry(user, () -> executor.downloadAsset(user, "/v2/credit-notes/" + number + "/download-pdf", "pdf_base_64_encoded"));
    }

    @CircuitBreaker(name = "factus", fallbackMethod = "factusUnavailableByteArrayFallback")
    public byte[] downloadXml(User user, String number) {
        return executor.executeWithRetry(user, () -> executor.downloadAsset(user, "/v2/credit-notes/" + number + "/download-xml", "xml_base_64_encoded"));
    }

    private Map<String, Object> factusUnavailableMapFallback(Exception ex) {
        String message = extractErrorMessage(ex);
        throw new ApiException(503, message, "FACTUS_CIRCUIT_OPEN");
    }

    private FactusCreditNoteResult factusUnavailableResultFallback(Exception ex) {
        String message = extractErrorMessage(ex);
        throw new ApiException(503, message, "FACTUS_CIRCUIT_OPEN");
    }

    private byte[] factusUnavailableByteArrayFallback(Exception ex) {
        String message = extractErrorMessage(ex);
        throw new ApiException(503, message, "FACTUS_CIRCUIT_OPEN");
    }

    private void factusUnavailableVoidFallback(Exception ex) {
        String message = extractErrorMessage(ex);
        throw new ApiException(503, message, "FACTUS_CIRCUIT_OPEN");
    }

    private String extractErrorMessage(Exception ex) {
        if (ex instanceof ApiException apiEx) {
            return apiEx.getMessage();
        }
        if (ex != null && ex.getMessage() != null) {
            return ex.getMessage();
        }
        return "Factus no disponible temporalmente, reintente en unos minutos";
    }

    // ----- Business Logic -----

    private FactusCreditNotePayloadDto buildPayload(CreditNote creditNote) {
        List<FactusCreditNotePaymentDto> paymentDetails = getPaymentDetails(creditNote);
        FactusCreditNoteCustomerDto customer = creditNote.getInvoice() != null ? getCustomer(creditNote) : null;
        List<FactusCreditNoteItemDto> items = getItems(creditNote);
        List<FactusCreditNoteAllowanceChargeDto> allowanceCharges = !creditNote.getAllowanceCharges().isEmpty()
                ? getAllowanceCharges(creditNote)
                : null;

        return new FactusCreditNotePayloadDto(
                creditNote.getReferenceCode(),
                creditNote.getCorrectionConceptCode(),
                creditNote.getCustomizationId(),
                creditNote.getObservation(),
                creditNote.getInvoice() != null ? creditNote.getInvoice().getFactusNumber() : null,
                customer,
                items,
                paymentDetails,
                allowanceCharges
        );
    }

    @NotNull
    private static List<FactusCreditNoteAllowanceChargeDto> getAllowanceCharges(CreditNote creditNote) {
        List<FactusCreditNoteAllowanceChargeDto> allowanceCharges = new ArrayList<>();
        for (CreditNoteAllowanceCharge ac : creditNote.getAllowanceCharges()) {
            FactusCreditNoteAllowanceChargeDto dto = new FactusCreditNoteAllowanceChargeDto(
                    ac.getConceptType(),
                    ac.isSurcharge(),
                    ac.getReason(),
                    ac.getBaseAmount(),
                    ac.getAmount()
            );
            allowanceCharges.add(dto);
        }
        return allowanceCharges;
    }

    @NotNull
    private static List<FactusCreditNoteItemDto> getItems(CreditNote creditNote) {
        List<FactusCreditNoteItemDto> items = new ArrayList<>();
        for (CreditNoteItem item : creditNote.getItems()) {
            List<FactusCreditNoteItemTaxDto> taxes = new ArrayList<>();
            List<FactusCreditNoteItemTaxDto> withholdingTaxes = new ArrayList<>();
            for (var tax : item.getTaxes()) {
                FactusCreditNoteItemTaxDto taxDto = new FactusCreditNoteItemTaxDto(
                        tax.getTaxCode(),
                        tax.getTaxRate()
                );
                if (tax.isWithholding()) {
                    withholdingTaxes.add(taxDto);
                } else {
                    taxes.add(taxDto);
                }
            }

            FactusCreditNoteItemDto dto = new FactusCreditNoteItemDto(
                    item.getCodeReference(),
                    item.getName(),
                    item.getQuantity(),
                    item.getPrice(),
                    item.getDiscountRate() != null && item.getDiscountRate().compareTo(java.math.BigDecimal.ZERO) > 0 ? item.getDiscountRate() : null,
                    item.getUnitMeasureCode(),
                    item.getStandardCode(),
                    item.getNote(),
                    !taxes.isEmpty() ? taxes : null,
                    !withholdingTaxes.isEmpty() ? withholdingTaxes : null
            );
            items.add(dto);
        }
        return items;
    }

    @NotNull
    private static FactusCreditNoteCustomerDto getCustomer(CreditNote creditNote) {
        var customer = creditNote.getInvoice().getCustomer();
        return new FactusCreditNoteCustomerDto(
                customer.getIdentification(),
                customer.getLegalOrgCode(),
                customer.getNames(),
                customer.getEmail(),
                customer.getPhone(),
                customer.getAddress(),
                customer.getMunicipalityCode()
        );
    }

    @NotNull
    private static List<FactusCreditNotePaymentDto> getPaymentDetails(CreditNote creditNote) {
        List<FactusCreditNotePaymentDto> paymentDetails = new ArrayList<>();
        for (CreditNotePayment payment : creditNote.getPayments()) {
            FactusCreditNotePaymentDto dto = new FactusCreditNotePaymentDto(
                    payment.getPaymentForm(),
                    payment.getPaymentMethodCode(),
                    payment.getReferenceCode(),
                    payment.getAmount(),
                    payment.getDueDate() != null ? payment.getDueDate().toString() : null
            );
            paymentDetails.add(dto);
        }
        return paymentDetails;
    }

    public static FactusCreditNoteResult parseResponse(Map<String, Object> response) {
        if (response == null) {
            return new FactusCreditNoteResult(null, null, null, false);
        }

        FactusCreditNoteResult result = new FactusCreditNoteResult(null, null, null, false);

        // Intentar extraer de response.data.credit_note
        Object data = response.get("data");
        if (data instanceof Map<?, ?> dataMap) {
            Object creditNote = dataMap.get("credit_note");
            if (creditNote instanceof Map<?, ?> creditNoteMap) {
                result = extractFields(creditNoteMap, result);
            }
        }

        // Si no encontramos número, intentar response.data.credit-note
        if (result.number() == null) {
            Object data2 = response.get("data");
            if (data2 instanceof Map<?, ?> dataMap) {
                Object creditNote = dataMap.get("credit-note");
                if (creditNote instanceof Map<?, ?> creditNoteMap) {
                    result = extractFields(creditNoteMap, result);
                }
            }
        }

        // Intentar directamente en response.credit_note
        if (result.number() == null) {
            Object creditNote = response.get("credit_note");
            if (creditNote instanceof Map<?, ?> creditNoteMap) {
                result = extractFields(creditNoteMap, result);
            }
        }

        // Intentar directamente en response (top-level)
        if (result.number() == null) {
            result = extractFields(response, result);
        }

        // Intentar extraer xmlUrl de links si no lo encontramos
        if (result.xmlUrl() == null) {
            Object links = response.get("links");
            if (links instanceof Map<?, ?> linksMap) {
                Object pubUrl = linksMap.get("public_url");
                if (pubUrl != null) {
                    result = new FactusCreditNoteResult(result.number(), result.cufe(), pubUrl.toString(), result.validated());
                }
            }
        }

        return result;
    }

    private static FactusCreditNoteResult extractFields(Map<?, ?> map, FactusCreditNoteResult result) {
        String number = result.number();
        String cufe = result.cufe();
        String xmlUrl = result.xmlUrl();
        boolean validated = result.validated();

        Object numObj = map.get("number");
        if (numObj != null) number = numObj.toString();

        Object cufeObj = map.get("cufe");
        if (cufeObj != null) cufe = cufeObj.toString();

        Object validatedObj = map.get("is_validated");
        if (validatedObj instanceof Boolean v) {
            validated = v;
        } else {
            Object validatedAlt = map.get("validated");
            if (validatedAlt instanceof Boolean v) validated = v;
        }

        Object xmlUrlObj = map.get("xml_url");
        if (xmlUrlObj != null) xmlUrl = xmlUrlObj.toString();

        return new FactusCreditNoteResult(number, cufe, xmlUrl, validated);
    }

    public record FactusCreditNoteResult(String number, String cufe, String xmlUrl, boolean validated) {}
}
