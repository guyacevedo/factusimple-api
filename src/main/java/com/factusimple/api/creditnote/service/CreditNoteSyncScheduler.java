package com.factusimple.api.creditnote.service;

import com.factusimple.api.creditnote.entity.CreditNote;
import com.factusimple.api.creditnote.repository.CreditNoteRepository;
import com.factusimple.api.infrastructure.exception.ResourceNotFoundException;
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
public class CreditNoteSyncScheduler {

    private final CreditNoteRepository creditNoteRepository;
    private final CreditNoteSyncLogic creditNoteSyncLogic;

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void syncWithFactusInNewTransaction(User user, UUID creditNoteId) {
        CreditNote creditNote = creditNoteRepository.findById(creditNoteId)
                .orElseThrow(() -> new ResourceNotFoundException("CreditNote", "id", creditNoteId.toString()));
        creditNoteSyncLogic.syncToFactusSafely(user, creditNote);
    }
}