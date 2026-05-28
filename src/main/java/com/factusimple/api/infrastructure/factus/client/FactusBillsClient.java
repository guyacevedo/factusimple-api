package com.factusimple.api.infrastructure.factus.client;

import com.factusimple.api.customer.entity.Customer;
import com.factusimple.api.establishment.entity.Establishment;
import com.factusimple.api.infrastructure.exception.ApiException;
import com.factusimple.api.infrastructure.factus.dto.bill.*;
import com.factusimple.api.invoice.entity.*;
import com.factusimple.api.user.entity.User;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
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
        FactusBillPayloadDto payload = buildPayload(invoice);
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

        FactusBillResult result = new FactusBillResult(null, null, null, false);
        Object data = response.get("data");
        if (data instanceof Map<?, ?> dataMap) {
            result = extractFields(dataMap, result);
        }

        if (result.number() == null) {
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
    private FactusBillPayloadDto buildPayload(Invoice invoice) {

        Establishment establishment = invoice.getEstablishment();
        Customer customer = invoice.getCustomer();

        FactusBillEstablishmentDto establishmentDto = getEstablishmentDto(establishment);
        FactusBillCustomerDto customerDto = getCustomerDto(customer);
        List<FactusBillItemDto> itemsList = getItemsList(invoice);
        List<FactusBillPaymentDto> paymentsList = getPaymentsList(invoice);

        List<FactusBillPrepaymentDto> prepaymentsList = !invoice.getPrepayments().isEmpty()
                ? getPrepaymentsList(invoice)
                : null;

        List<FactusBillAllowanceChargeDto> allowanceChargesList = !invoice.getAllowanceCharges().isEmpty()
                ? getAllowanceChargeList(invoice)
                : null;

        return new FactusBillPayloadDto(
                invoice.getNumberingRangeId() != null ? invoice.getNumberingRangeId().toString() : null,
                invoice.getReferenceCode(),
                invoice.getDocumentType(),
                invoice.getOperationType(),
                invoice.getSendEmail(),
                invoice.getObservation(),
                invoice.getCashRounding(),
                establishmentDto,
                customerDto,
                itemsList,
                paymentsList,
                prepaymentsList,
                allowanceChargesList
        );
    }

    private static List<FactusBillAllowanceChargeDto> getAllowanceChargeList(Invoice invoice) {
        List<FactusBillAllowanceChargeDto> allowanceChargeList = new java.util.ArrayList<>();
        for (AllowanceCharge allowanceCharge : invoice.getAllowanceCharges()) {
            FactusBillAllowanceChargeDto dto = new FactusBillAllowanceChargeDto(
                    allowanceCharge.getConceptType(),
                    allowanceCharge.getIsSurcharge(),        // true = recargo, false = descuento
                    allowanceCharge.getReason(),
                    allowanceCharge.getBaseAmount(),
                    allowanceCharge.getAmount()
            );
            allowanceChargeList.add(dto);
        }
        return allowanceChargeList;
    }

    private static List<FactusBillPrepaymentDto> getPrepaymentsList(Invoice invoice) {
        List<FactusBillPrepaymentDto> prepaymentsList = new java.util.ArrayList<>();
        for (InvoicePrepayment pp : invoice.getPrepayments()) {
            FactusBillPrepaymentDto dto = new FactusBillPrepaymentDto(
                    pp.getReferenceCode(),
                    pp.getReceivedDate().toString(),
                    pp.getAmount(),
                    pp.getNote()
            );
            prepaymentsList.add(dto);
        }
        return prepaymentsList;
    }

    private static List<FactusBillPaymentDto> getPaymentsList(Invoice invoice) {
        List<FactusBillPaymentDto> paymentsList = new java.util.ArrayList<>();
        for (InvoicePayment p : invoice.getPayments()) {
            FactusBillPaymentDto dto = new FactusBillPaymentDto(
                    p.getPaymentForm(),
                    p.getPaymentMethodCode(),
                    p.getAmount(),
                    p.getReferenceCode(),
                    p.getDueDate() != null ? p.getDueDate().toString() : null
            );
            paymentsList.add(dto);
        }
        return paymentsList;
    }

    private static List<FactusBillItemDto> getItemsList(Invoice invoice) {

        List<FactusBillItemDto> itemsList = new java.util.ArrayList<>();

        for (InvoiceItem invoiceItem : invoice.getItems()) {

            FactusBillItemDto item = getItem(invoiceItem);

            itemsList.add(item);
        }

        return itemsList;
    }

    private static FactusBillItemDto getItem(InvoiceItem invoiceItem) {
        // code_reference: use provided value or fall back to product SKU
        String codeRef = invoiceItem.getCodeReference();
        if (codeRef == null && invoiceItem.getProduct() != null) {
            codeRef = invoiceItem.getProduct().getSku();
        }
        if (codeRef == null) {
            throw new ApiException(400, "El invoiceItem requiere codeReference o un producto con SKU definido");
        }

        String sku = null;
        if (invoiceItem.getProduct() != null && invoiceItem.getProduct().getSku() != null) {
            sku = invoiceItem.getProduct().getSku();
        }

        // Separar impuestos vs retenciones
        List<FactusBillItemTaxDto> taxes = new java.util.ArrayList<>();
        List<FactusBillItemTaxDto> withholdings = new java.util.ArrayList<>();
        for (InvoiceItemTax tax : invoiceItem.getTaxes()) {
            FactusBillItemTaxDto t = new FactusBillItemTaxDto(
                    tax.getTaxCode(),
                    tax.getTaxRate()
            );
            if (Boolean.TRUE.equals(tax.getIsWithholding())) {
                withholdings.add(t);
            } else {
                taxes.add(t);
            }
        }

        return new FactusBillItemDto(
                codeRef,
                invoiceItem.getName(),
                invoiceItem.getQuantity(),
                invoiceItem.getUnitPrice(),
                invoiceItem.getDiscountRate() != null ? invoiceItem.getDiscountRate() : java.math.BigDecimal.ZERO,
                invoiceItem.getUnitMeasureCode(),
                invoiceItem.getStandardCode(),
                invoiceItem.getNote(),
                sku,
                !taxes.isEmpty() ? taxes : null,
                !withholdings.isEmpty() ? withholdings : null
        );
    }

    private static FactusBillEstablishmentDto getEstablishmentDto(Establishment establishment) {
        return new FactusBillEstablishmentDto(
                establishment.getName(),
                establishment.getAddress(),
                establishment.getPhoneNumber(),
                establishment.getEmail(),
                establishment.getMunicipalityCode()
        );
    }

    private static FactusBillCustomerDto getCustomerDto(Customer customer) {
        return new FactusBillCustomerDto(
                customer.getIdTypeCode(),
                customer.getIdentification(),
                customer.getDv(),
                customer.getLegalOrgCode(),
                customer.getTributeCode(),
                customer.getNames(),
                customer.getCompany(),
                customer.getTradeName(),
                customer.getAddress(),
                customer.getEmail(),
                customer.getPhone(),
                customer.getMunicipalityCode()
        );
    }

    /**
     * Parsea la respuesta de /v2/bills/validate.
     * Intenta múltiples estructuras posibles:
     * 1. response.data (si data es un Map con number, cufe, etc.)
     * 2. response.data.bill (estructura anidada)
     * 3. response.bill (directamente)
     */
    public static FactusBillResult parseBillResponse(Map<String, Object> response) {
        FactusBillResult result = new FactusBillResult(null, null, null, false);
        if (response == null) return result;

        // Intentar en response.data si es un Map
        Object data = response.get("data");
        if (data instanceof Map<?, ?> dataMap) {
            result = extractFields(dataMap, result);

            // Si no encontramos número, intentar response.data.bill
            if (result.number() == null) {
                Object bill = dataMap.get("bill");
                if (bill instanceof Map<?, ?> billMap) {
                    result = extractFields(billMap, result);
                }
            }
        }

        // Intentar directamente en response.bill
        if (result.number() == null) {
            Object bill = response.get("bill");
            if (bill instanceof Map<?, ?> billMap) {
                result = extractFields(billMap, result);
            }
        }

        // Intentar extraer xmlUrl de links si no lo encontramos
        if (result.xmlUrl() == null) {
            Object links = response.get("links");
            if (links instanceof Map<?, ?> linksMap) {
                Object pub = linksMap.get("public_url");
                if (pub != null) result = new FactusBillResult(result.number(), result.cufe(), pub.toString(), result.validated());
            }
        }

        return result;
    }

    private static FactusBillResult extractFields(Map<?, ?> map, FactusBillResult result) {
        String number = result.number();
        String cufe = result.cufe();
        String xmlUrl = result.xmlUrl();
        boolean validated = result.validated();

        Object numberObj = map.get("number");
        if (numberObj != null) number = numberObj.toString();

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

        return new FactusBillResult(number, cufe, xmlUrl, validated);
    }

    public record FactusBillResult(String number, String cufe, String xmlUrl, boolean validated) {
        public static FactusBillResult from(String number, String cufe, String xmlUrl, boolean validated) {
            return new FactusBillResult(number, cufe, xmlUrl, validated);
        }
    }
}
