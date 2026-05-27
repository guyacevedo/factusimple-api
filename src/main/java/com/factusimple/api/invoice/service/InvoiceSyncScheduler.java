package com.factusimple.api.invoice.service;

import com.factusimple.api.infrastructure.exception.ResourceNotFoundException;
import com.factusimple.api.invoice.entity.Invoice;
import com.factusimple.api.invoice.repository.InvoiceRepository;
import com.factusimple.api.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class InvoiceSyncScheduler {

    private final InvoiceRepository invoiceRepository;
    private final InvoiceSyncLogic invoiceSyncLogic;

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void syncWithFactusInNewTransaction(User user, UUID invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice", "id", invoiceId));
        invoiceSyncLogic.syncToFactusSafely(user, invoice);
    }
}