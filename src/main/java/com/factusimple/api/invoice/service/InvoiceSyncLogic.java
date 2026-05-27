package com.factusimple.api.invoice.service;

import com.factusimple.api.infrastructure.exception.ApiException;
import com.factusimple.api.infrastructure.factus.client.FactusBillsClient;
import com.factusimple.api.invoice.entity.Invoice;
import com.factusimple.api.invoice.entity.InvoiceStatus;
import com.factusimple.api.invoice.repository.InvoiceRepository;
import com.factusimple.api.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class InvoiceSyncLogic {

    private final FactusBillsClient factusBillsClient;
    private final InvoiceRepository invoiceRepository;

    public void syncToFactusSafely(User user, Invoice invoice) {
        try {
            if (invoice.getFactusNumber() != null) {
                var complete = factusBillsClient.getBillDetailsFromFactus(user, invoice.getFactusNumber());
                invoice.setCufe(complete.cufe());
                invoice.setXmlUrl(complete.xmlUrl());
                invoice.setStatus(complete.validated() ? InvoiceStatus.VALIDATED : InvoiceStatus.PENDING);
                invoice.setFactusError(null);
                invoiceRepository.save(invoice);
                log.info("Factura {} ya existía en Factus, datos refrescados", invoice.getId());
                return;
            }

            var response = factusBillsClient.createBillAtFactus(invoice);
            var parsed = FactusBillsClient.parseBillResponse(response);

            if (parsed.number() != null) {
                var complete = factusBillsClient.getBillDetailsFromFactus(user, parsed.number());
                invoice.setFactusNumber(complete.number());
                invoice.setCufe(complete.cufe());
                invoice.setXmlUrl(complete.xmlUrl());
                invoice.setStatus(complete.validated() ? InvoiceStatus.VALIDATED : InvoiceStatus.PENDING);
            } else {
                invoice.setFactusNumber(null);
                invoice.setCufe(parsed.cufe());
                invoice.setXmlUrl(parsed.xmlUrl());
                invoice.setStatus(parsed.validated() ? InvoiceStatus.VALIDATED : InvoiceStatus.PENDING);
            }

            invoice.setFactusError(null);
            invoiceRepository.save(invoice);
            log.info("Factura sincronizada con Factus: id={}, factusNumber={}, status={}",
                    invoice.getId(), invoice.getFactusNumber(), invoice.getStatus());
        } catch (Exception e) {
            log.error("Sync con Factus falló para invoice {}: {}", invoice.getId(), e.getMessage(), e);
            invoice.setStatus(InvoiceStatus.ERROR);
            invoice.setFactusError(truncate(extractDetailedError(e)));
            invoiceRepository.save(invoice);
        }
    }

    private static String truncate(String value) {
        if (value == null) return null;
        return value.length() <= 2000 ? value : value.substring(0, 2000);
    }

    private static String extractDetailedError(Exception ex) {
        if (ex instanceof ApiException apiEx) {
            return apiEx.getMessage();
        }
        if (ex != null && ex.getMessage() != null && !ex.getMessage().isEmpty()) {
            return ex.getMessage();
        }
        if (ex != null && ex.getCause() != null && ex.getCause().getMessage() != null) {
            return ex.getCause().getMessage();
        }
        return ex != null ? ex.getClass().getSimpleName() : "Unknown error";
    }
}