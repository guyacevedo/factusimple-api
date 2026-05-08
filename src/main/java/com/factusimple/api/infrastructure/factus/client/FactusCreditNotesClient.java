package com.factusimple.api.infrastructure.factus.client;

import com.factusimple.api.auth.entity.Token;
import com.factusimple.api.auth.repository.TokenRepository;
import com.factusimple.api.creditnote.entity.CreditNote;
import com.factusimple.api.creditnote.entity.CreditNoteAllowanceCharge;
import com.factusimple.api.creditnote.entity.CreditNoteItem;
import com.factusimple.api.creditnote.entity.CreditNotePayment;
import com.factusimple.api.infrastructure.exception.ApiException;
import com.factusimple.api.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class FactusCreditNotesClient {

    private final RestClient restClient;
    private final TokenRepository tokenRepository;
    private final FactusAuthClient factusAuthClient;


    public Map<String, Object> createAtFactus(CreditNote creditNote) {
        User user = creditNote.getEstablishment().getUser();
        String token = getToken(user);
        Map<String, Object> payload = buildPayload(creditNote);
        return restClient.post()
                .uri( "/v2/credit-notes/validate")
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/json")
                .body(payload)
                .retrieve()
                .body(Map.class);
    }

    public FactusCreditNoteResult getDetailsFromFactus(User user, String number) {
        String token = getToken(user);
        Map<String, Object> response = restClient.method(HttpMethod.GET)
                .uri( "/v2/credit-notes/{number}", number)
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .body(Map.class);
        return parseResponse(response);
    }

    public void deleteAtFactus(User user, String referenceCode) {
        try {
            String token = getToken(user);
            restClient.method(HttpMethod.DELETE)
                    .uri( "/v2/credit-notes/reference/{referenceCode}", referenceCode)
                    .header("Authorization", "Bearer " + token)
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            log.warn("Failed to delete credit note from Factus (reference: {}): {}", referenceCode, e.getMessage());
        }
    }

    public byte[] downloadPdf(User user, String number) {
        String token = getToken(user);
        byte[] pdf =restClient.method(HttpMethod.GET)
                .uri( "/v2/credit-notes/{number}/download-pdf", number)
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .body(byte[].class);
        return pdf != null ? pdf : new byte[0];
    }

    public byte[] downloadXml(User user, String number) {
        String token = getToken(user);
        byte[] xml = restClient.method(HttpMethod.GET)
                .uri( "/v2/credit-notes/{number}/download-xml", number)
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .body(byte[].class);
        return xml != null ? xml : new byte[0];
    }

    public static FactusCreditNoteResult parseResponse(Map<String, Object> response) {
        if (response == null) {
            return new FactusCreditNoteResult(null, null, null, false);
        }
        String number = (String) response.get("number");
        String cufe = (String) response.get("cufe");
        String xmlUrl = (String) response.get("xml_url");
        Boolean validated = (Boolean) response.getOrDefault("validated", false);
        return new FactusCreditNoteResult(number, cufe, xmlUrl, validated != null && validated);
    }

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

    private String getToken(User user) {
        Optional<Token> tokenOpt = tokenRepository.findAllByUserAndRevokedAndTokenType(user, false, Token.TokenType.ACCESS);

        if (tokenOpt.isEmpty()) {
            refreshAndSaveToken(user);
            tokenOpt = tokenRepository.findAllByUserAndRevokedAndTokenType(user, false, Token.TokenType.ACCESS);
        }

        if (tokenOpt.isEmpty()) {
            throw new ApiException(502, "Token no encontrado para Factus");
        }

        return tokenOpt.get().getToken();
    }

    private void refreshAndSaveToken(User user) {
        var authResponse = factusAuthClient.generateToken();
        if (authResponse == null || authResponse.getAccess_token() == null) {
            throw new ApiException(502, "No se pudo refrescar el token de Factus");
        }
        tokenRepository.revokeAllByUser(user);
        Token newToken = new Token();
        newToken.setUser(user);
        newToken.setToken(authResponse.getAccess_token());
        newToken.setTokenType(Token.TokenType.ACCESS);
        newToken.setRevoked(false);
        tokenRepository.save(newToken);
        log.info("Token Factus refrescado para usuario: {}", user.getEmail());
    }

    public record FactusCreditNoteResult(String number, String cufe, String xmlUrl, boolean validated) {}
}
