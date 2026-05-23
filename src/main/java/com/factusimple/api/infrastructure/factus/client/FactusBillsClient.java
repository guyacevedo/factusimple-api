package com.factusimple.api.infrastructure.factus.client;

import com.factusimple.api.customer.entity.Customer;
import com.factusimple.api.establishments.entity.Establishment;
import com.factusimple.api.infrastructure.exception.ApiException;
import com.factusimple.api.invoice.entity.*;
import com.factusimple.api.user.entity.User;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Cliente de la API Factus. Encapsula:
 *  - Construcción del payload /v2/bills/validate desde la entidad Invoice.
 *  - Decodificación de PDF/XML base64.
 *  - Reintentos y autenticación delegados a FactusHttpExecutor.
 *
 * Las llamadas son síncronas y deben invocarse desde el InvoiceService
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class FactusBillsClient {

    private final FactusHttpExecutor executor;

    /**
     * Crea la factura en Factus y devuelve la respuesta cruda
     * (estructura {status, data: {bill: {...}}, links: {...}}).
     * Lanza ApiException si tras los reintentos no hubo éxito.
     */
    @SuppressWarnings("unchecked")
    @CircuitBreaker(name = "factus", fallbackMethod = "factusUnavailableMapFallback")
    public Map<String, Object> createBillAtFactus(Invoice invoice) {
        Map<String, Object> payload = buildPayload(invoice);
        User user = invoice.getEstablishment().getUser();
        log.debug("Enviando factura a Factus: referenceCode={} creada por usuario: {}", invoice.getReferenceCode(), user.getEmail());
        return executor.executeWithRetry(user, () -> executor.postJson(user, "/v2/bills/validate", payload, Map.class));
    }

    /**
     * Obtiene la factura en Factus y devuelve la respuesta cruda
     * (estructura {status, data: {bill: {...}}, links: {...}}).
     * Lanza ApiException si tras los reintentos no hubo éxito.
     */
    @SuppressWarnings("unchecked")
    @CircuitBreaker(name = "factus", fallbackMethod = "factusUnavailableMapFallback")
    public Map<String, Object> getBillFromFactus(User user, String factusNumber) {
        return executor.executeWithRetry(user, () -> executor.getJson(user, "/v2/bills/" + factusNumber, Map.class));
    }

    /**
     * Obtiene los datos completos de una factura desde Factus y los mapea a FactusBillResult.
     * Estructura esperada: response.data = { number, cufe, is_validated, ... }
     */
    @SuppressWarnings("unchecked")
    @CircuitBreaker(name = "factus", fallbackMethod = "factusUnavailableResultFallback")
    public FactusBillResult getBillDetailsFromFactus(User user, String factusNumber) {
        Map<String, Object> response = getBillFromFactus(user, factusNumber);

        FactusBillResult result = new FactusBillResult();
        Object data = response.get("data");
        if (data instanceof Map<?, ?> dataMap) {
            extractFields(dataMap, result);
        }

        if (result.getNumber() == null) {
            throw new ApiException(502, "No se pudo obtener el número de factura de Factus");
        }

        return result;
    }

    @CircuitBreaker(name = "factus", fallbackMethod = "factusUnavailableByteArrayFallback")
    public byte[] downloadPdf(User user, String factusNumber) {
        return executor.executeWithRetry(user, () -> executor.downloadAsset(user, "/v2/bills/" + factusNumber + "/download-pdf", "pdf_base_64_encoded"));
    }

    @CircuitBreaker(name = "factus", fallbackMethod = "factusUnavailableByteArrayFallback")
    public byte[] downloadXml(User user, String factusNumber) {
        return executor.executeWithRetry(user, () -> executor.downloadAsset(user, "/v2/bills/" + factusNumber + "/download-xml", "xml_base_64_encoded"));
    }

    @CircuitBreaker(name = "factus", fallbackMethod = "factusUnavailableVoidFallback")
    public void deleteBillAtFactus(User user, String referenceCode) {
        executor.executeWithRetry(user, () -> {
            executor.deleteJson(user, "/v2/bills/destroy/reference/" + referenceCode);
            return null;
        });
    }

    private Map<String, Object> factusUnavailableMapFallback(Exception ex) {
        String message = extractErrorMessage(ex);
        throw new ApiException(503, message, "FACTUS_CIRCUIT_OPEN");
    }

    private FactusBillResult factusUnavailableResultFallback(Exception ex) {
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

    /**
     * Construye el payload de /v2/bills/validate.
     * Los nombres de campo siguen la convención snake_case de Factus.
     * Ajustar aquí si la sandbox rechaza por nombres específicos.
     */
    private Map<String, Object> buildPayload(Invoice invoice) {

        Establishment establishment = invoice.getEstablishment();
        Customer customer = invoice.getCustomer();

        Map<String, Object> payload = new LinkedHashMap<>();

        payload.put("numbering_range_id", establishment.getNumberingRangeId());
        payload.put("reference_code", invoice.getReferenceCode());
        payload.put("document_type", invoice.getDocumentType());
        payload.put("operation_type", invoice.getOperationType());
        payload.put("send_email", invoice.getSendEmail());
        if (invoice.getObservation() != null) {
            payload.put("observation", invoice.getObservation());
        }
        if (invoice.getCashRounding() != null) {
            payload.put("cash_rounding", invoice.getCashRounding());
        }

        // Establishment
        Map<String, Object> establishmentMap = getEstablishmentMap(establishment);
        payload.put("establishment", establishmentMap);

        // Customer
        Map<String, Object> customerMap = getCustomerMap(customer);
        payload.put("customer", customerMap);

        // Items
        List<Map<String, Object>> itemsList = getItemsList(invoice);
        payload.put("items", itemsList);

        // Payments
        List<Map<String, Object>> paymentsList = getPaymentsList(invoice);
        payload.put("payment_details", paymentsList);

        // Prepayments
        if (!invoice.getPrepayments().isEmpty()) {
            List<Map<String, Object>> prepaymentsList = getPrepaymentsList(invoice);
            payload.put("prepayments", prepaymentsList);
        }

        // Allowance/Charges
        if (!invoice.getAllowanceCharges().isEmpty()) {
            List<Map<String, Object>> acsList = getAllowanceChargeList(invoice);
            payload.put("allowance_charges", acsList);
        }

        return payload;
    }

    private static List<Map<String, Object>> getAllowanceChargeList(Invoice invoice) {
        List<Map<String, Object>> allowanceChargeList = new java.util.ArrayList<>();
        for (AllowanceCharge allowanceCharge : invoice.getAllowanceCharges()) {
            Map<String, Object> acm = new LinkedHashMap<>();
            acm.put("concept_type", allowanceCharge.getConceptType());
            acm.put("is_charge", allowanceCharge.getIsSurcharge());        // true = recargo, false = descuento
            acm.put("reason", allowanceCharge.getReason());
            acm.put("base_amount", allowanceCharge.getBaseAmount());
            acm.put("amount", allowanceCharge.getAmount());
            allowanceChargeList.add(acm);
        }
        return allowanceChargeList;
    }

    private static List<Map<String, Object>> getPrepaymentsList(Invoice invoice) {
        List<Map<String, Object>> prepaymentsList = new java.util.ArrayList<>();
        for (InvoicePrepayment pp : invoice.getPrepayments()) {
            Map<String, Object> ppm = new LinkedHashMap<>();
            if (pp.getReferenceCode() != null) ppm.put("reference_code", pp.getReferenceCode());
            ppm.put("received_date", pp.getReceivedDate().toString());
            ppm.put("amount", pp.getAmount());
            if (pp.getNote() != null) ppm.put("note", pp.getNote());
            prepaymentsList.add(ppm);
        }
        return prepaymentsList;
    }

    private static List<Map<String, Object>> getPaymentsList(Invoice invoice) {
        List<Map<String, Object>> paymentsList = new java.util.ArrayList<>();
        for (InvoicePayment p : invoice.getPayments()) {
            Map<String, Object> pm = new LinkedHashMap<>();
            pm.put("payment_form", p.getPaymentForm());
            pm.put("payment_method_code", p.getPaymentMethodCode());
            pm.put("amount", p.getAmount());
            if (p.getReferenceCode() != null) pm.put("reference_code", p.getReferenceCode());
            if (p.getDueDate() != null) pm.put("due_date", p.getDueDate().toString());
            paymentsList.add(pm);
        }
        return paymentsList;
    }

    private static List<Map<String, Object>> getItemsList(Invoice invoice) {

        List<Map<String, Object>> itemsList = new java.util.ArrayList<>();

        for (InvoiceItem invoiceItem : invoice.getItems()) {

            Map<String, Object> item = getItem(invoiceItem);

            itemsList.add(item);
        }

        return itemsList;
    }

    private static Map<String, Object> getItem(InvoiceItem invoiceItem) {
        Map<String, Object> item = new LinkedHashMap<>();
        // code_reference: use provided value or fall back to product SKU
        String codeRef = invoiceItem.getCodeReference();
        if (codeRef == null && invoiceItem.getProduct() != null) {
            codeRef = invoiceItem.getProduct().getSku();
        }
        if (codeRef == null) {
            throw new ApiException(400, "El invoiceItem requiere codeReference o un producto con SKU definido");
        }
        item.put("code_reference", codeRef);
        item.put("name", invoiceItem.getName());
        item.put("quantity", invoiceItem.getQuantity());
        item.put("price", invoiceItem.getUnitPrice());
        // discount_rate always required by Factus; default to 0
        item.put("discount_rate", invoiceItem.getDiscountRate() != null ? invoiceItem.getDiscountRate() : java.math.BigDecimal.ZERO);
        if (invoiceItem.getUnitMeasureCode() != null) item.put("unit_measure_code", invoiceItem.getUnitMeasureCode());
        if (invoiceItem.getStandardCode() != null) item.put("standard_code", invoiceItem.getStandardCode());
        if (invoiceItem.getNote() != null) item.put("note", invoiceItem.getNote());
        if (invoiceItem.getProduct() != null && invoiceItem.getProduct().getSku() != null) {
            item.put("sku", invoiceItem.getProduct().getSku());
        }

        // Separar impuestos vs retenciones
        List<Map<String, Object>> taxes = new java.util.ArrayList<>();
        List<Map<String, Object>> withholdings = new java.util.ArrayList<>();
        for (InvoiceItemTax tax : invoiceItem.getTaxes()) {
            Map<String, Object> t = new LinkedHashMap<>();
            t.put("code", tax.getTaxCode());
            t.put("rate", tax.getTaxRate());
            if (Boolean.TRUE.equals(tax.getIsWithholding())) {
                withholdings.add(t);
            } else {
                taxes.add(t);
            }
        }
        if (!taxes.isEmpty()) item.put("taxes", taxes);
        if (!withholdings.isEmpty()) item.put("withholding_taxes", withholdings);
        return item;
    }

    private static Map<String, Object> getEstablishmentMap(Establishment establishment) {
        Map<String, Object> establishmentMap = new LinkedHashMap<>();
        establishmentMap.put("name", establishment.getName());
        establishmentMap.put("address", establishment.getAddress());
        if (establishment.getPhoneNumber() != null) establishmentMap.put("phone_number", establishment.getPhoneNumber());
        if (establishment.getEmail() != null) establishmentMap.put("email", establishment.getEmail());
        if (establishment.getMunicipalityCode() != null) {
            establishmentMap.put("municipality_code", establishment.getMunicipalityCode());
        }
        return establishmentMap;
    }

    private static Map<String, Object> getCustomerMap(Customer customer) {
        Map<String, Object> customerMap = new LinkedHashMap<>();
        customerMap.put("identification_document_code", customer.getIdTypeCode());
        customerMap.put("identification", customer.getIdentification());
        if (customer.getDv() != null) customerMap.put("dv", customer.getDv());
        customerMap.put("legal_organization_id", customer.getLegalOrgCode());
        customerMap.put("tribute_id", customer.getTributeCode());
        customerMap.put("names", customer.getNames());
        if (customer.getCompany() != null) customerMap.put("company", customer.getCompany());
        if (customer.getTradeName() != null) customerMap.put("trade_name", customer.getTradeName());
        if (customer.getAddress() != null) customerMap.put("address", customer.getAddress());
        if (customer.getEmail() != null) customerMap.put("email", customer.getEmail());
        if (customer.getPhone() != null) customerMap.put("phone", customer.getPhone());
        if (customer.getMunicipalityCode() != null) {
            customerMap.put("municipality_id", customer.getMunicipalityCode());
        }
        return customerMap;
    }

    /**
     * Parsea la respuesta de /v2/bills/validate.
     * Intenta múltiples estructuras posibles:
     * 1. response.data (si data es un Map con number, cufe, etc.)
     * 2. response.data.bill (estructura anidada)
     * 3. response.bill (directamente)
     */
    public static FactusBillResult parseBillResponse(Map<String, Object> response) {
        FactusBillResult result = new FactusBillResult();
        if (response == null) return result;

        // Intentar en response.data si es un Map
        Object data = response.get("data");
        if (data instanceof Map<?, ?> dataMap) {
            extractFields(dataMap, result);

            // Si no encontramos número, intentar response.data.bill
            if (result.getNumber() == null) {
                Object bill = dataMap.get("bill");
                if (bill instanceof Map<?, ?> billMap) {
                    extractFields(billMap, result);
                }
            }
        }

        // Intentar directamente en response.bill
        if (result.getNumber() == null) {
            Object bill = response.get("bill");
            if (bill instanceof Map<?, ?> billMap) {
                extractFields(billMap, result);
            }
        }

        // Intentar extraer xmlUrl de links si no lo encontramos
        if (result.getXmlUrl() == null) {
            Object links = response.get("links");
            if (links instanceof Map<?, ?> linksMap) {
                Object pub = linksMap.get("public_url");
                if (pub != null) result.setXmlUrl(pub.toString());
            }
        }

        return result;
    }

    private static void extractFields(Map<?, ?> map, FactusBillResult result) {
        Object number = map.get("number");
        if (number != null) result.setNumber(number.toString());

        Object cufe = map.get("cufe");
        if (cufe != null) result.setCufe(cufe.toString());

        Object validated = map.get("is_validated");
        if (validated instanceof Boolean v) {
            result.setValidated(v);
        } else {
            Object validatedAlt = map.get("validated");
            if (validatedAlt instanceof Boolean v) result.setValidated(v);
        }

        Object xmlUrl = map.get("xml_url");
        if (xmlUrl != null) result.setXmlUrl(xmlUrl.toString());
    }

    @Setter
    @Getter
    public static class FactusBillResult {
        private String number;
        private String cufe;
        private String xmlUrl;
        private boolean validated;

    }
}
