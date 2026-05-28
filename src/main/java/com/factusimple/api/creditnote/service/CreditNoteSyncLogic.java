package com.factusimple.api.creditnote.service;

import com.factusimple.api.creditnote.entity.CreditNote;
import com.factusimple.api.creditnote.entity.CreditNoteStatus;
import com.factusimple.api.creditnote.repository.CreditNoteRepository;
import com.factusimple.api.infrastructure.exception.ApiException;
import com.factusimple.api.infrastructure.factus.client.FactusCreditNotesClient;
import com.factusimple.api.invoice.entity.InvoiceStatus;
import com.factusimple.api.invoice.repository.InvoiceRepository;
import com.factusimple.api.product.repository.ProductRepository;
import com.factusimple.api.user.entity.User;
import com.factusimple.api.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class CreditNoteSyncLogic {

    private final FactusCreditNotesClient factusCreditNotesClient;
    private final CreditNoteRepository creditNoteRepository;
    private final InvoiceRepository invoiceRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    public void syncToFactusSafely(User user, CreditNote creditNote) {
        try {
            if (creditNote.getFactusNumber() != null) {
                var details = factusCreditNotesClient.getDetailsFromFactus(user, creditNote.getFactusNumber());
                creditNote.setStatus(details.validated() ? CreditNoteStatus.VALIDATED : CreditNoteStatus.PENDING);
                return;
            }

            Map<String, Object> response = factusCreditNotesClient.createAtFactus(creditNote);
            var result = FactusCreditNotesClient.parseResponse(response);

            if (result.number() != null) {
                var details = factusCreditNotesClient.getDetailsFromFactus(user, result.number());
                creditNote.setFactusNumber(details.number());
                creditNote.setCufe(details.cufe());
                creditNote.setXmlUrl(details.xmlUrl());
                creditNote.setStatus(details.validated() ? CreditNoteStatus.VALIDATED : CreditNoteStatus.PENDING);
            }
            creditNote.setFactusError(null);

            if (creditNote.getStatus() == CreditNoteStatus.VALIDATED && creditNote.getInvoice() != null) {
                var invoice = creditNote.getInvoice();
                invoice.setStatus(InvoiceStatus.CANCELLED);
                invoiceRepository.save(invoice);
                userRepository.decrementInvoiceCount(invoice.getEstablishment().getUser().getId());

                if ("1".equals(creditNote.getCorrectionConceptCode()) || "2".equals(creditNote.getCorrectionConceptCode())) {
                    restoreStock(creditNote.getItems());
                }
            }

            creditNoteRepository.save(creditNote);
        } catch (Exception e) {
            creditNote.setStatus(CreditNoteStatus.ERROR);
            creditNote.setFactusError(truncate(extractDetailedError(e)));
            creditNoteRepository.save(creditNote);
            log.error("Error syncing credit note with Factus: {}", e.getMessage(), e);
        }
    }

    private void restoreStock(java.util.List<com.factusimple.api.creditnote.entity.CreditNoteItem> items) {
        for (var item : items) {
            if (item.getProduct() == null || item.getProduct().getStock() == null) {
                continue;
            }
            productRepository.incrementStock(item.getProduct().getId(), item.getQuantity());
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