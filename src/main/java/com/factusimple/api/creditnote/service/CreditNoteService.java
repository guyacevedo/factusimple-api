package com.factusimple.api.creditnote.service;

import com.factusimple.api.creditnote.dto.CreditNoteRequestDto;
import com.factusimple.api.creditnote.dto.CreditNoteResponseDto;
import com.factusimple.api.creditnote.entity.CreditNote;
import com.factusimple.api.creditnote.entity.CreditNoteItem;
import com.factusimple.api.creditnote.entity.CreditNoteStatus;
import com.factusimple.api.creditnote.mapper.CreditNoteMapper;
import com.factusimple.api.creditnote.repository.CreditNoteRepository;
import com.factusimple.api.establishment.entity.Establishment;
import com.factusimple.api.establishment.service.EstablishmentService;
import com.factusimple.api.infrastructure.exception.BadRequestException;
import com.factusimple.api.infrastructure.exception.ConflictException;
import com.factusimple.api.infrastructure.exception.ResourceNotFoundException;
import com.factusimple.api.infrastructure.factus.client.FactusCreditNotesClient;
import com.factusimple.api.invoice.entity.Invoice;
import com.factusimple.api.invoice.entity.InvoiceStatus;
import com.factusimple.api.invoice.repository.InvoiceRepository;
import com.factusimple.api.product.repository.ProductRepository;
import com.factusimple.api.user.entity.User;
import com.factusimple.api.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CreditNoteService {

    private final CreditNoteRepository creditNoteRepository;
    private final CreditNoteMapper creditNoteMapper;
    private final InvoiceRepository invoiceRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final EstablishmentService establishmentService;
    private final FactusCreditNotesClient factusCreditNotesClient;
    private final EntityManager entityManager;
    private final CreditNoteSyncScheduler creditNoteSyncScheduler;

    @Transactional
    public CreditNoteResponseDto create(UUID userId, CreditNoteRequestDto dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId.toString()));

        Establishment establishment = establishmentService.getEntityByUserId(userId);

        if ("20".equals(dto.customizationId()) || dto.customizationId() == null) {
            if (dto.invoiceId() == null) {
                throw new BadRequestException("invoiceId es requerido cuando customizationId es '20'");
            }
            Invoice invoice = invoiceRepository.findByIdAndEstablishmentId(dto.invoiceId(), establishment.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Invoice", "id", dto.invoiceId().toString()));
            if (invoice.getStatus() != InvoiceStatus.VALIDATED) {
                throw new ConflictException("La factura debe estar en estado VALIDATED para crear una nota de crédito");
            }
        }

        if (creditNoteRepository.existsByReferenceCodeAndEstablishmentId(dto.referenceCode(), establishment.getId())) {
            throw new ConflictException("Ya existe una nota de crédito con referenceCode: " + dto.referenceCode());
        }

        CreditNote creditNote = creditNoteMapper.toEntity(dto);
        creditNote.setEstablishment(establishment);
        if (dto.invoiceId() != null) {
            Invoice invoice = invoiceRepository.findByIdAndEstablishmentId(dto.invoiceId(), establishment.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Invoice", "id", dto.invoiceId().toString()));
            creditNote.setInvoice(invoice);
        }
        creditNote.setCustomizationId(dto.customizationId() != null ? dto.customizationId() : "20");

        creditNote.setItems(creditNoteMapper.itemsToEntityList(dto.items()));
        var savedItems = creditNote.getItems();
        var dtoItems = dto.items();
        for (int i = 0; i < savedItems.size(); i++) {
            var item = savedItems.get(i);
            item.setCreditNote(creditNote);
            if (item.getProduct() != null) {
                var product = productRepository.findById(item.getProduct().getId())
                        .orElseThrow(() -> new ResourceNotFoundException("Product", "id", item.getProduct().getId().toString()));
                item.setProduct(product);
            }
            item.setTaxes(creditNoteMapper.itemTaxesToEntityList(dtoItems.get(i).taxes()));
            for (var tax : item.getTaxes()) {
                tax.setItem(item);
            }
        }

        creditNote.setPayments(creditNoteMapper.paymentsToEntityList(dto.payments()));
        for (var payment : creditNote.getPayments()) {
            payment.setCreditNote(creditNote);
        }

        if (dto.allowanceCharges() != null && !dto.allowanceCharges().isEmpty()) {
            creditNote.setAllowanceCharges(creditNoteMapper.allowanceChargesToEntityList(dto.allowanceCharges()));
            for (var ac : creditNote.getAllowanceCharges()) {
                ac.setCreditNote(creditNote);
            }
        }

        creditNote = creditNoteRepository.save(creditNote);
        entityManager.flush();
        creditNoteSyncScheduler.syncWithFactusInNewTransaction(user, creditNote.getId());

        return creditNoteMapper.toDto(creditNoteRepository.save(creditNote));
    }

    @Transactional
    public CreditNoteResponseDto syncWithFactus(UUID userId, UUID cnId) {
        Establishment establishment = establishmentService.getEntityByUserId(userId);
        CreditNote creditNote = requireOwned(cnId, establishment.getId());

        if (creditNote.getStatus() == CreditNoteStatus.VALIDATED) {
            throw new ConflictException("No se puede sincronizar una nota de crédito ya validada");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId.toString()));
        creditNoteSyncScheduler.syncWithFactusInNewTransaction(user, cnId);
        creditNote = requireOwned(cnId, establishment.getId());
        return creditNoteMapper.toDto(creditNote);
    }

    @Transactional
    public void delete(UUID userId, UUID cnId) {
        Establishment establishment = establishmentService.getEntityByUserId(userId);
        CreditNote creditNote = requireOwned(cnId, establishment.getId());

        if (creditNote.getStatus() == CreditNoteStatus.VALIDATED) {
            throw new ConflictException("No se puede eliminar una nota de crédito validada");
        }

        if (creditNote.getFactusNumber() != null) {
            try {
                User user = userRepository.findById(userId)
                        .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId.toString()));
                factusCreditNotesClient.deleteAtFactus(user, creditNote.getReferenceCode());
            } catch (Exception e) {
                log.warn("Error deleting credit note from Factus: {}", e.getMessage());
            }
        }

        creditNoteRepository.delete(creditNote);
    }

    @Transactional(readOnly = true)
    public Page<CreditNoteResponseDto> list(UUID userId, CreditNoteStatus status, Pageable pageable) {
        Establishment establishment = establishmentService.getEntityByUserId(userId);

        Page<CreditNote> page;
        if (status != null) {
            page = creditNoteRepository.findByEstablishmentIdAndStatus(establishment.getId(), status, pageable);
        } else {
            page = creditNoteRepository.findByEstablishmentId(establishment.getId(), pageable);
        }

        return page.map(creditNoteMapper::toDto);
    }

    @Transactional(readOnly = true)
    public CreditNoteResponseDto get(UUID userId, UUID cnId) {
        Establishment establishment = establishmentService.getEntityByUserId(userId);
        CreditNote creditNote = requireOwned(cnId, establishment.getId());
        return creditNoteMapper.toDto(creditNote);
    }

    public byte[] downloadPdf(UUID userId, UUID cnId) {
        Establishment establishment = establishmentService.getEntityByUserId(userId);
        CreditNote creditNote = requireOwned(cnId, establishment.getId());

        if (creditNote.getStatus() != CreditNoteStatus.VALIDATED || creditNote.getFactusNumber() == null) {
            throw new ConflictException("Solo se pueden descargar PDFs de notas de crédito validadas");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId.toString()));
        return factusCreditNotesClient.downloadPdf(user, creditNote.getFactusNumber());
    }

    public byte[] downloadXml(UUID userId, UUID cnId) {
        Establishment establishment = establishmentService.getEntityByUserId(userId);
        CreditNote creditNote = requireOwned(cnId, establishment.getId());

        if (creditNote.getStatus() != CreditNoteStatus.VALIDATED || creditNote.getFactusNumber() == null) {
            throw new ConflictException("Solo se pueden descargar XMLs de notas de crédito validadas");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId.toString()));
        return factusCreditNotesClient.downloadXml(user, creditNote.getFactusNumber());
    }

    private void restoreStock(java.util.List<CreditNoteItem> items) {
        for (CreditNoteItem item : items) {
            if (item.getProduct() == null || item.getProduct().getStock() == null) {
                continue;
            }
            productRepository.incrementStock(item.getProduct().getId(), item.getQuantity());
        }
    }

    private CreditNote requireOwned(UUID cnId, UUID establishmentId) {
        return creditNoteRepository.findByIdAndEstablishmentId(cnId, establishmentId)
                .orElseThrow(() -> new ResourceNotFoundException("CreditNote", "id", cnId.toString()));
    }
}
