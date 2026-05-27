package com.factusimple.api.invoice.service;


import com.factusimple.api.customer.entity.Customer;
import com.factusimple.api.customer.repository.CustomerRepository;
import com.factusimple.api.establishment.entity.Establishment;
import com.factusimple.api.establishment.service.EstablishmentService;
import com.factusimple.api.infrastructure.exception.*;
import com.factusimple.api.infrastructure.factus.client.FactusBillsClient;
import com.factusimple.api.infrastructure.factus.codes.InvoiceDocumentType;
import com.factusimple.api.infrastructure.factus.codes.InvoiceOperationType;
import com.factusimple.api.infrastructure.factus.codes.TaxCode;
import com.factusimple.api.infrastructure.factus.codes.WithholdingTaxCode;
import com.factusimple.api.invoice.dto.*;
import com.factusimple.api.invoice.entity.*;
import com.factusimple.api.shared.dto.ItemTaxRequestDto;
import com.factusimple.api.shared.dto.PaymentRequestDto;
import com.factusimple.api.invoice.mapper.InvoiceMapper;
import com.factusimple.api.invoice.repository.InvoiceRepository;
import com.factusimple.api.product.entity.Product;
import com.factusimple.api.product.repository.ProductRepository;
import com.factusimple.api.user.entity.User;
import com.factusimple.api.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class InvoiceService {

    private static final BigDecimal HUNDRED = new BigDecimal("100");
    private static final int SCALE = 2;

    private static final Set<String> VALID_TAX_CODES = Arrays.stream(TaxCode.values())
            .map(TaxCode::getCode).collect(Collectors.toUnmodifiableSet());
    private static final Set<String> VALID_WITHHOLDING_CODES = Arrays.stream(WithholdingTaxCode.values())
            .map(WithholdingTaxCode::getCode).collect(Collectors.toUnmodifiableSet());

    private final InvoiceRepository invoiceRepository;
    private final InvoiceMapper invoiceMapper;
    private final EstablishmentService establishmentService;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final FactusBillsClient factusBillsClient;
    private final EntityManager entityManager;

    @Transactional
    public InvoiceResponseDto create(UUID userId, InvoiceRequestDto requestDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        validatePayments(requestDto.payments());
        validateItemTaxes(requestDto.items());

        Invoice saved = createInvoiceLocal(userId, requestDto);

        // Atomic increment: if limit reached, returns 0 (no update)
        int updated = userRepository.incrementInvoiceCountIfBelowLimit(userId, user.getPlan().getMaxInvoices());
        if (updated == 0) {
            throw new ForbiddenException(
                    "Límite del plan alcanzado: " + user.getPlan().getMaxInvoices() + " facturas");
        }

        // Sync síncrono con Factus. Errores no abortan la creación local
        // (la factura queda con status=ERROR + factusError para poder reintentar).
        syncToFactusSafely(user, saved);

        return invoiceMapper.toDto(saved);
    }

    @Transactional
    private Invoice createInvoiceLocal(UUID userId, InvoiceRequestDto requestDto) {
        Establishment establishment = establishmentService.getEntityByUserId(userId);

        if (invoiceRepository.existsByReferenceCodeAndEstablishmentId(
                requestDto.referenceCode(), establishment.getId())) {
            throw new ConflictException(
                    "Ya existe una factura con referenceCode '" + requestDto.referenceCode()
                            + "' en este establecimiento");
        }

        Customer customer = customerRepository
                .findByIdAndEstablishmentId(requestDto.customerId(), establishment.getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Customer", "id", requestDto.customerId()));

        Invoice invoice = invoiceMapper.toEntity(requestDto);
        invoice.setEstablishment(establishment);
        invoice.setCustomer(customer);
        invoice.setStatus(InvoiceStatus.PENDING);
        invoice.setDocumentType(InvoiceDocumentType.FACTURA_VENTA.getCode());
        invoice.setOperationType(requestDto.operationType() != null ? requestDto.operationType() :InvoiceOperationType.ESTANDAR.getCode());
        invoice.setSendEmail((requestDto.sendEmail() != null) && requestDto.sendEmail());
        attachItems(invoice, requestDto.items(), establishment.getId());
        attachPayments(invoice, requestDto.payments());
        attachPrepayments(invoice, requestDto.prepayments());
        attachAllowanceCharges(invoice, requestDto.allowanceCharges());

        applyTotals(invoice);
        validateStockAvailability(invoice.getItems());

        Invoice saved = invoiceRepository.save(invoice);
        entityManager.flush();
        deductStock(saved.getItems());

        log.info("Factura creada: id={}, referenceCode={}, total={}",
                saved.getId(), saved.getReferenceCode(), saved.getTotal());

        return saved;
    }

    /**
     * Reintenta el envío a Factus para una factura PENDING o ERROR.
     * No incrementa invoiceCount (eso ocurre solo en create()).
     */
    public InvoiceResponseDto syncWithFactus(UUID userId, UUID invoiceId) {
        Invoice invoice = getAndValidateForSync(userId, invoiceId);
        syncToFactusSafely(invoice.getEstablishment().getUser(), invoice);
        return invoiceMapper.toDto(invoice);
    }

    @Transactional
    private Invoice getAndValidateForSync(UUID userId, UUID invoiceId) {
        Invoice invoice = requireOwned(userId, invoiceId);
        if (invoice.getStatus() == InvoiceStatus.VALIDATED) {
            throw new ConflictException("La factura ya está validada en Factus");
        }
        if (invoice.getStatus() == InvoiceStatus.CANCELLED) {
            throw new ConflictException("La factura está cancelada");
        }
        return invoice;
    }

    public InvoiceResponseDto cancel(UUID userId, UUID invoiceId) {
        Invoice invoice = getAndValidateForCancel(userId, invoiceId);

        if (invoice.getFactusNumber() != null) {
            try {
                factusBillsClient.deleteBillAtFactus(
                        invoice.getEstablishment().getUser(), invoice.getReferenceCode());
            } catch (Exception e) {
                log.warn("No se pudo eliminar factura en Factus al cancelar (referenceCode={}): {}",
                        invoice.getReferenceCode(), e.getMessage());
            }
        }

        markAsCancelledInTransaction(userId, invoiceId);
        log.info("Factura cancelada: id={}", invoiceId);

        invoice = requireOwned(userId, invoiceId);
        return invoiceMapper.toDto(invoice);
    }

    @Transactional
    private Invoice getAndValidateForCancel(UUID userId, UUID invoiceId) {
        Invoice invoice = requireOwned(userId, invoiceId);

        if (invoice.getStatus() == InvoiceStatus.VALIDATED) {
            throw new ConflictException(
                    "Las facturas validadas no pueden cancelarse directamente; emita una nota crédito");
        }
        if (invoice.getStatus() == InvoiceStatus.CANCELLED) {
            throw new ConflictException("La factura ya está cancelada");
        }

        restoreStock(invoice.getItems());
        return invoice;
    }

    @Transactional
    private void markAsCancelledInTransaction(UUID userId, UUID invoiceId) {
        Invoice invoice = requireOwned(userId, invoiceId);
        invoice.setStatus(InvoiceStatus.CANCELLED);
        invoiceRepository.save(invoice);
        userRepository.decrementInvoiceCount(userId);
    }

    @Transactional
    public byte[] downloadPdf(UUID userId, UUID invoiceId) {
        Invoice invoice = requireValidated(userId, invoiceId);
        return factusBillsClient.downloadPdf(invoice.getEstablishment().getUser(), invoice.getFactusNumber());
    }

    @Transactional
    public byte[] downloadXml(UUID userId, UUID invoiceId) {
        Invoice invoice = requireValidated(userId, invoiceId);
        return factusBillsClient.downloadXml(invoice.getEstablishment().getUser(), invoice.getFactusNumber());
    }

    private void syncToFactusSafely(User user, Invoice invoice) {
        try {
            if (invoice.getFactusNumber() != null) {
                var complete = factusBillsClient.getBillDetailsFromFactus(user, invoice.getFactusNumber());
                invoice.setCufe(complete.cufe());
                invoice.setXmlUrl(complete.xmlUrl());
                invoice.setStatus(complete.validated() ? InvoiceStatus.VALIDATED : InvoiceStatus.PENDING);
                invoice.setFactusError(null);
                saveInvoiceInTransaction(invoice);
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
            saveInvoiceInTransaction(invoice);
            log.info("Factura sincronizada con Factus: id={}, factusNumber={}, status={}",
                    invoice.getId(), invoice.getFactusNumber(), invoice.getStatus());
        } catch (Exception e) {
            log.error("Sync con Factus falló para invoice {}: {}", invoice.getId(), e.getMessage(), e);
            invoice.setStatus(InvoiceStatus.ERROR);
            invoice.setFactusError(truncate(extractDetailedError(e)));
            saveInvoiceInTransaction(invoice);
        }
    }

    @Transactional
    private void saveInvoiceInTransaction(Invoice invoice) {
        invoiceRepository.save(invoice);
    }

    private Invoice requireValidated(UUID userId, UUID invoiceId) {
        Invoice invoice = requireOwned(userId, invoiceId);
        if (invoice.getStatus() != InvoiceStatus.VALIDATED || invoice.getFactusNumber() == null) {
            throw new ConflictException(
                    "La factura no está validada en Factus (status=" + invoice.getStatus() + ")");
        }
        return invoice;
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

    @Transactional(readOnly = true)
    public InvoiceResponseDto get(UUID userId, UUID invoiceId) {
        return invoiceMapper.toDto(requireOwned(userId, invoiceId));
    }

    @Transactional(readOnly = true)
    public Page<InvoiceResponseDto> list(UUID userId, InvoiceStatus status, UUID customerId, Pageable pageable) {
        Establishment establishment = establishmentService.getEntityByUserId(userId);
        Page<Invoice> page;
        if (status != null && customerId != null) {
            page = invoiceRepository.findByEstablishmentIdAndStatusAndCustomerId(
                    establishment.getId(), status, customerId, pageable);
        } else if (status != null) {
            page = invoiceRepository.findByEstablishmentIdAndStatus(
                    establishment.getId(), status, pageable);
        } else if (customerId != null) {
            page = invoiceRepository.findByEstablishmentIdAndCustomerId(
                    establishment.getId(), customerId, pageable);
        } else {
            page = invoiceRepository.findByEstablishmentId(establishment.getId(), pageable);
        }
        return page.map(invoiceMapper::toDto);
    }

    public void delete(UUID userId, UUID invoiceId) {
        Invoice invoice = getAndValidateForDelete(userId, invoiceId);

        if (invoice.getFactusNumber() != null) {
            try {
                factusBillsClient.deleteBillAtFactus(invoice.getEstablishment().getUser(),invoice.getReferenceCode());
            } catch (Exception e) {
                log.warn("No se pudo eliminar factura en Factus (referenceCode={}): {}",
                        invoice.getReferenceCode(), e.getMessage());
            }
        }

        deleteInvoiceInTransaction(userId, invoiceId);
        log.info("Factura eliminada: id={}", invoiceId);
    }

    @Transactional
    private Invoice getAndValidateForDelete(UUID userId, UUID invoiceId) {
        Invoice invoice = requireOwned(userId, invoiceId);
        if (invoice.getStatus() == InvoiceStatus.VALIDATED) {
            throw new ApiException(409,
                    "No se puede eliminar una factura validada en Factus; emita una nota crédito");
        }

        if (invoice.getStatus() != InvoiceStatus.CANCELLED) {
            restoreStock(invoice.getItems());
        }
        return invoice;
    }

    @Transactional
    private void deleteInvoiceInTransaction(UUID userId, UUID invoiceId) {
        Invoice invoice = requireOwned(userId, invoiceId);
        invoiceRepository.delete(invoice);
        userRepository.decrementInvoiceCount(userId);
    }

    private Invoice requireOwned(UUID userId, UUID invoiceId) {
        Establishment establishment = establishmentService.getEntityByUserId(userId);
        return invoiceRepository.findByIdAndEstablishmentId(invoiceId, establishment.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Invoice", "id", invoiceId));
    }

    // ----- Validaciones cruzadas -----

    private void validatePayments(List<PaymentRequestDto> payments) {
        for (PaymentRequestDto p : payments) {
            // validar
            if (p.paymentForm().equals("2") && p.dueDate() == null) {
                throw new BadRequestException(
                        "Pagos a crédito (paymentForm='2') requieren dueDate");
            }
        }
    }

    private void validateItemTaxes(List<InvoiceItemRequestDto> items) {
        for (int i = 0; i < items.size(); i++) {
            List<ItemTaxRequestDto> taxes = items.get(i).taxes();
            for (ItemTaxRequestDto tax : taxes) {
                boolean withholding = Boolean.TRUE.equals(tax.isWithholding());
                Set<String> allowed = withholding ? VALID_WITHHOLDING_CODES : VALID_TAX_CODES;
                if (!allowed.contains(tax.taxCode())) {
                    throw new BadRequestException(
                            "Item #" + (i + 1) + ": código '" + tax.taxCode()
                                    + "' inválido para " + (withholding ? "retención" : "impuesto")
                                    + ". Valores aceptados: " + String.join(", ", allowed));
                }
            }
        }
    }

    // ----- Wiring de hijos a la Invoice -----

    private void attachItems(Invoice invoice, List<InvoiceItemRequestDto> dtos, UUID establishmentId) {
        List<InvoiceItem> items = invoiceMapper.itemsToEntity(dtos);
        for (int i = 0; i < items.size(); i++) {
            InvoiceItem item = items.get(i);
            InvoiceItemRequestDto dto = dtos.get(i);

            item.setInvoice(invoice);
            if (dto.productId() != null) {
                Product product = productRepository
                        .findByIdAndEstablishmentId(dto.productId(), establishmentId)
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Product", "id", dto.productId()));
                item.setProduct(product);
            }
            for (InvoiceItemTax tax : item.getTaxes()) {
                tax.setItem(item);
                if (tax.getIsWithholding() == null) {
                    tax.setIsWithholding(false);
                }
            }
        }
        invoice.getItems().addAll(items);
    }

    private void attachPayments(Invoice invoice, List<PaymentRequestDto> dtos) {
        List<InvoicePayment> payments = invoiceMapper.paymentsToEntity(dtos);
        payments.forEach(p -> p.setInvoice(invoice));
        invoice.getPayments().addAll(payments);
    }

    private void attachPrepayments(Invoice invoice, List<InvoicePrepaymentRequestDto> dtos) {
        if (dtos == null || dtos.isEmpty()) return;
        List<InvoicePrepayment> prepayments = invoiceMapper.prepaymentsToEntity(dtos);
        prepayments.forEach(p -> p.setInvoice(invoice));
        invoice.getPrepayments().addAll(prepayments);
    }

    private void attachAllowanceCharges(Invoice invoice, List<AllowanceChargeRequestDto> dtos) {
        if (dtos == null || dtos.isEmpty()) return;
        List<AllowanceCharge> acs = invoiceMapper.allowanceChargesToEntity(dtos);
        acs.forEach(ac -> ac.setInvoice(invoice));
        invoice.getAllowanceCharges().addAll(acs);
    }

    // ----- Cálculo de totales -----

    /**
     * Reglas:
     * - itemSubtotal = quantity × unitPrice
     * - itemDiscount = itemSubtotal × discountRate / 100
     * - taxableBase = itemSubtotal − itemDiscount
     * - itemTaxes = Σ(taxableBase × taxRate / 100) sólo no-retenciones
     * - subtotal = Σ itemSubtotal
     * - totalDiscounts = Σ itemDiscount + Σ allowanceCharges (no-surcharge)
     * - totalTaxes = Σ itemTaxes
     * - totalSurcharges = Σ allowanceCharges (surcharge)
     * - totalPrepayments = Σ prepayments
     * - total = (subtotal − totalDiscounts) + totalTaxes + totalSurcharges − totalPrepayments + cashRounding
     * <p>
     * Las retenciones se registran pero no afectan el total del documento (Factus las maneja aparte).
     */
    private void applyTotals(Invoice invoice) {
        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal totalItemDiscounts = BigDecimal.ZERO;
        BigDecimal totalTaxes = BigDecimal.ZERO;

        for (InvoiceItem item : invoice.getItems()) {
            BigDecimal itemSubtotal = item.getQuantity().multiply(item.getUnitPrice());
            BigDecimal itemDiscount = item.getDiscountRate() == null
                    ? BigDecimal.ZERO
                    : itemSubtotal.multiply(item.getDiscountRate()).divide(HUNDRED, SCALE, RoundingMode.HALF_UP);
            BigDecimal taxableBase = itemSubtotal.subtract(itemDiscount);

            BigDecimal itemTaxes = BigDecimal.ZERO;
            for (InvoiceItemTax tax : item.getTaxes()) {
                if (Boolean.TRUE.equals(tax.getIsWithholding())) continue;
                itemTaxes = itemTaxes.add(
                        taxableBase.multiply(tax.getTaxRate()).divide(HUNDRED, SCALE, RoundingMode.HALF_UP));
            }

            subtotal = subtotal.add(itemSubtotal);
            totalItemDiscounts = totalItemDiscounts.add(itemDiscount);
            totalTaxes = totalTaxes.add(itemTaxes);
        }

        BigDecimal totalSurcharges = BigDecimal.ZERO;
        BigDecimal totalAllowances = BigDecimal.ZERO;
        for (AllowanceCharge ac : invoice.getAllowanceCharges()) {
            if (Boolean.TRUE.equals(ac.getIsSurcharge())) {
                totalSurcharges = totalSurcharges.add(ac.getAmount());
            } else {
                totalAllowances = totalAllowances.add(ac.getAmount());
            }
        }

        BigDecimal totalPrepayments = invoice.getPrepayments().stream()
                .map(InvoicePrepayment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalDiscounts = totalItemDiscounts.add(totalAllowances);
        BigDecimal cashRounding = invoice.getCashRounding() != null ? invoice.getCashRounding() : BigDecimal.ZERO;

        BigDecimal total = subtotal
                .subtract(totalDiscounts)
                .add(totalTaxes)
                .add(totalSurcharges)
                .subtract(totalPrepayments)
                .add(cashRounding);

        invoice.setSubtotal(subtotal.setScale(SCALE, RoundingMode.HALF_UP));
        invoice.setTotalDiscounts(totalDiscounts.setScale(SCALE, RoundingMode.HALF_UP));
        invoice.setTotalTaxes(totalTaxes.setScale(SCALE, RoundingMode.HALF_UP));
        invoice.setTotal(total.setScale(SCALE, RoundingMode.HALF_UP));
    }

    // ----- Stock Management -----

    private void validateStockAvailability(List<InvoiceItem> items) {
        for (InvoiceItem item : items) {
            if (item.getProduct() == null || item.getProduct().getStock() == null) continue;
            if (item.getProduct().getStock().compareTo(item.getQuantity()) < 0) {
                throw new UnprocessableEntityException(
                        "Stock insuficiente para el producto '" + item.getProduct().getSku()
                        + "': disponible " + item.getProduct().getStock()
                        + ", requerido " + item.getQuantity());
            }
        }
    }

    private void deductStock(List<InvoiceItem> items) {
        for (InvoiceItem item : items) {
            if (item.getProduct() == null || item.getProduct().getStock() == null) continue;
            int updated = productRepository.decrementStock(
                    item.getProduct().getId(), item.getQuantity());
            if (updated == 0) {
                throw new ConflictException(
                        "Stock insuficiente para el producto '" + item.getProduct().getSku()
                        + "' (modificado por otra operación concurrente)");
            }
        }
    }

    private void restoreStock(List<InvoiceItem> items) {
        for (InvoiceItem item : items) {
            if (item.getProduct() == null || item.getProduct().getStock() == null) continue;
            productRepository.incrementStock(item.getProduct().getId(), item.getQuantity());
        }
    }
}
