package com.factusimple.api.invoice.repository;

import com.factusimple.api.invoice.entity.Invoice;
import com.factusimple.api.invoice.entity.InvoiceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, UUID> {

    Optional<Invoice> findByIdAndEstablishmentId(UUID id, UUID establishmentId);

    Page<Invoice> findByEstablishmentId(UUID establishmentId, Pageable pageable);

    Page<Invoice> findByEstablishmentIdAndStatus(
            UUID establishmentId, InvoiceStatus status, Pageable pageable);

    Page<Invoice> findByEstablishmentIdAndCustomerId(
            UUID establishmentId, UUID customerId, Pageable pageable);

    Page<Invoice> findByEstablishmentIdAndStatusAndCustomerId(
            UUID establishmentId, InvoiceStatus status, UUID customerId, Pageable pageable);

    boolean existsByReferenceCodeAndEstablishmentId(String referenceCode, UUID establishmentId);
}
