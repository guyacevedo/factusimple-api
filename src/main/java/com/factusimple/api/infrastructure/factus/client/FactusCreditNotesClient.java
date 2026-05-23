package com.factusimple.api.infrastructure.factus.client;

import com.factusimple.api.creditnote.entity.CreditNote;
import com.factusimple.api.creditnote.entity.CreditNoteAllowanceCharge;
import com.factusimple.api.creditnote.entity.CreditNoteItem;
import com.factusimple.api.creditnote.entity.CreditNotePayment;
import com.factusimple.api.infrastructure.exception.ApiException;
import com.factusimple.api.user.entity.User;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class FactusCreditNotesClient {

    private final FactusHttpExecutor executor;


    @CircuitBreaker(name = "factus", fallbackMethod = "factusUnavailableMapFallback")
    public Map<String, Object> createAtFactus(CreditNote creditNote) {
        User user = creditNote.getEstablishment().getUser();
        Map<String, Object> payload = buildPayload(creditNote);
        return executor.executeWithRetry(user, () -> executor.postJson(user, "/v2/credit-notes/validate", payload, Map.class));
    }

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
        return executor.executeWithRetry(user, () -> {
            byte[] pdf = executor.getJson(user, "/v2/credit-notes/{number}/download-pdf", byte[].class, number);
            return pdf != null ? pdf : new byte[0];
        });
    }

    @CircuitBreaker(name = "factus", fallbackMethod = "factusUnavailableByteArrayFallback")
    public byte[] downloadXml(User user, String number) {
        return executor.executeWithRetry(user, () -> {
            byte[] xml = executor.getJson(user, "/v2/credit-notes/{number}/download-xml", byte[].class, number);
            return xml != null ? xml : new byte[0];
        });
    }

    private Map<String, Object> factusUnavailableMapFallback(Exception ex) {
        throw new ApiException(503, "Factus no disponible temporalmente, reintente en unos minutos", "FACTUS_CIRCUIT_OPEN");
    }

    private FactusCreditNoteResult factusUnavailableResultFallback(Exception ex) {
        throw new ApiException(503, "Factus no disponible temporalmente, reintente en unos minutos", "FACTUS_CIRCUIT_OPEN");
    }

    private byte[] factusUnavailableByteArrayFallback(Exception ex) {
        throw new ApiException(503, "Factus no disponible temporalmente, reintente en unos minutos", "FACTUS_CIRCUIT_OPEN");
    }

    private void factusUnavailableVoidFallback(Exception ex) {
        throw new ApiException(503, "Factus no disponible temporalmente, reintente en unos minutos", "FACTUS_CIRCUIT_OPEN");
    }

    // ----- Business Logic -----

    private Map<String, Object> buildPayload(CreditNote creditNote) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("reference_code", creditNote.getReferenceCode());
        payload.put("correction_concept_code", creditNote.getCorrectionConceptCode());
        payload.put("customization_id", creditNote.getCustomizationId());

        if (creditNote.getObservation() != null) {
            payload.put("observation", creditNote.getObservation());
        }

        if (creditNote.getInvoice() != null) {
            payload.put("bill_number", creditNote.getInvoice().getFactusNumber());
        }

        List<Map<String, Object>> paymentDetails = new ArrayList<>();
        for (CreditNotePayment payment : creditNote.getPayments()) {
            Map<String, Object> pd = new LinkedHashMap<>();
            pd.put("payment_form", payment.getPaymentForm());
            pd.put("payment_method_code", payment.getPaymentMethodCode());
            if (payment.getReferenceCode() != null) {
                pd.put("reference_code", payment.getReferenceCode());
            }
            pd.put("amount", payment.getAmount());
            if (payment.getDueDate() != null) {
                pd.put("due_date", payment.getDueDate().toString());
            }
            paymentDetails.add(pd);
        }
        payload.put("payment_details", paymentDetails);

        if (creditNote.getInvoice() != null) {
            Map<String, Object> customer = new LinkedHashMap<>();
            customer.put("identification", creditNote.getInvoice().getCustomer().getIdentification());
            customer.put("legal_org_code", creditNote.getInvoice().getCustomer().getLegalOrgCode());
            customer.put("names", creditNote.getInvoice().getCustomer().getNames());
            customer.put("email", creditNote.getInvoice().getCustomer().getEmail());
            customer.put("phone", creditNote.getInvoice().getCustomer().getPhone());
            customer.put("address", creditNote.getInvoice().getCustomer().getAddress());
            customer.put("municipality_code", creditNote.getInvoice().getCustomer().getMunicipalityCode());
            payload.put("customer", customer);
        }

        List<Map<String, Object>> items = new ArrayList<>();
        for (CreditNoteItem item : creditNote.getItems()) {
            Map<String, Object> itemMap = new LinkedHashMap<>();
            if (item.getCodeReference() != null) {
                itemMap.put("code_reference", item.getCodeReference());
            }
            itemMap.put("name", item.getName());
            itemMap.put("quantity", item.getQuantity());
            itemMap.put("price", item.getPrice());
            if (item.getDiscountRate() != null && item.getDiscountRate().compareTo(java.math.BigDecimal.ZERO) > 0) {
                itemMap.put("discount_rate", item.getDiscountRate());
            }
            if (item.getUnitMeasureCode() != null) {
                itemMap.put("unit_measure_code", item.getUnitMeasureCode());
            }
            if (item.getStandardCode() != null) {
                itemMap.put("standard_code", item.getStandardCode());
            }
            if (item.getNote() != null) {
                itemMap.put("note", item.getNote());
            }

            List<Map<String, Object>> taxes = new ArrayList<>();
            List<Map<String, Object>> withholdingTaxes = new ArrayList<>();
            for (var tax : item.getTaxes()) {
                Map<String, Object> taxMap = new LinkedHashMap<>();
                taxMap.put("code", tax.getTaxCode());
                taxMap.put("rate", tax.getTaxRate());
                if (tax.isWithholding()) {
                    withholdingTaxes.add(taxMap);
                } else {
                    taxes.add(taxMap);
                }
            }
            if (!taxes.isEmpty()) {
                itemMap.put("taxes", taxes);
            }
            if (!withholdingTaxes.isEmpty()) {
                itemMap.put("withholding_taxes", withholdingTaxes);
            }

            items.add(itemMap);
        }
        payload.put("items", items);

        if (!creditNote.getAllowanceCharges().isEmpty()) {
            List<Map<String, Object>> allowanceCharges = new ArrayList<>();
            for (CreditNoteAllowanceCharge ac : creditNote.getAllowanceCharges()) {
                Map<String, Object> acMap = new LinkedHashMap<>();
                acMap.put("concept_type", ac.getConceptType());
                acMap.put("is_surcharge", ac.isSurcharge());
                if (ac.getReason() != null) {
                    acMap.put("reason", ac.getReason());
                }
                if (ac.getBaseAmount() != null) {
                    acMap.put("base_amount", ac.getBaseAmount());
                }
                acMap.put("amount", ac.getAmount());
                allowanceCharges.add(acMap);
            }
            payload.put("allowance_charges", allowanceCharges);
        }

        return payload;
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
