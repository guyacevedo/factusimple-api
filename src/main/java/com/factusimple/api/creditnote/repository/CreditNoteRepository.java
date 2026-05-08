package com.factusimple.api.creditnote.repository;

import com.factusimple.api.creditnote.entity.CreditNote;
import com.factusimple.api.creditnote.entity.CreditNoteStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CreditNoteRepository extends JpaRepository<CreditNote, UUID> {

    Optional<CreditNote> findByIdAndEstablishmentId(UUID id, UUID establishmentId);

    Page<CreditNote> findByEstablishmentId(UUID establishmentId, Pageable pageable);

    Page<CreditNote> findByEstablishmentIdAndStatus(UUID establishmentId, CreditNoteStatus status, Pageable pageable);

    boolean existsByReferenceCode(String referenceCode);
}
