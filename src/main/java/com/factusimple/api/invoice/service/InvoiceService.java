package com.factusimple.api.invoice.service;


import com.factusimple.api.customer.entity.Customer;
import com.factusimple.api.customer.repository.CustomerRepository;
import com.factusimple.api.establishments.entity.Establishment;
import com.factusimple.api.establishments.service.EstablishmentService;
import com.factusimple.api.infrastructure.exception.ApiException;
import com.factusimple.api.infrastructure.exception.ResourceNotFoundException;
import com.factusimple.api.infrastructure.factus.codes.TaxCode;
import com.factusimple.api.infrastructure.factus.codes.WithholdingTaxCode;
import com.factusimple.api.invoice.dto.*;
import com.factusimple.api.invoice.entity.*;
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

    private static final String DEFAULT_DOCUMENT_TYPE = "01";
    private static final String DEFAULT_OPERATION_TYPE = "10";
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

    @Transactional
    public InvoiceResponseDto create(UUID userId, InvoiceRequestDto requestDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        if (user.getInvoiceCount() >= user.getPlan().getMaxInvoices()) {
            throw new ApiException(403,
                    "Límite del plan alcanzado: " + user.getPlan().getMaxInvoices() + " facturas");
        }

        Establishment establishment = establishmentService.getEntityByUserId(userId);

        if (invoiceRepository.existsByReferenceCodeAndEstablishmentId(
                requestDto.getReferenceCode(), establishment.getId())) {
            throw new ApiException(409,
                    "Ya existe una factura con referenceCode '" + requestDto.getReferenceCode()
                            + "' en este establecimiento");
        }

        Customer customer = customerRepository
                .findByIdAndEstablishmentId(requestDto.getCustomerId(), establishment.getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Customer", "id", requestDto.getCustomerId()));

        validatePayments(requestDto.getPayments());
        validateItemTaxes(requestDto.getItems());

        Invoice invoice = invoiceMapper.toEntity(requestDto);
        invoice.setEstablishment(establishment);
        invoice.setCustomer(customer);
        invoice.setStatus(InvoiceStatus.PENDING);
        invoice.setDocumentType(
                requestDto.getDocumentType() != null ? requestDto.getDocumentType() : DEFAULT_DOCUMENT_TYPE);
        invoice.setOperationType(
                requestDto.getOperationType() != null ? requestDto.getOperationType() : DEFAULT_OPERATION_TYPE);
        invoice.setSendEmail(requestDto.getSendEmail() != null ? requestDto.getSendEmail() : false);

        attachItems(invoice, requestDto.getItems(), establishment.getId());
        attachPayments(invoice, requestDto.getPayments());
        attachPrepayments(invoice, requestDto.getPrepayments());
        attachAllowanceCharges(invoice, requestDto.getAllowanceCharges());

        applyTotals(invoice);

        Invoice saved = invoiceRepository.save(invoice);

        user.setInvoiceCount(user.getInvoiceCount() + 1);
        userRepository.save(user);

        log.info("Factura creada: id={}, referenceCode={}, total={}",
                saved.getId(), saved.getReferenceCode(), saved.getTotal());

        // Sync síncrono con Factus. Errores no abortan la creación local
        // (la factura queda con status=ERROR + factusError para poder reintentar).

        return invoiceMapper.toDto(saved);
    }

    /**
     * Reintenta el envío a Factus para una factura PENDING o ERROR.
     * No incrementa invoiceCount (eso ocurre solo en create()).
     */
    @Transactional
    public InvoiceResponseDto syncWithFactus(UUID userId, UUID invoiceId) {
        Invoice invoice = requireOwned(userId, invoiceId);
        if (invoice.getStatus() == InvoiceStatus.VALIDATED) {
            throw new ApiException(409, "La factura ya está validada en Factus");
        }
        if (invoice.getStatus() == InvoiceStatus.CANCELLED) {
            throw new ApiException(409, "La factura está cancelada");
        }
        //sincronizar con factus
        return invoiceMapper.toDto(invoice);
    }

    @Transactional(readOnly = true)
    public byte[] downloadPdf(UUID userId, UUID invoiceId) {
        Invoice invoice = requireValidated(userId, invoiceId);
        return null;
    }

    @Transactional(readOnly = true)
    public byte[] downloadXml(UUID userId, UUID invoiceId) {
        Invoice invoice = requireValidated(userId, invoiceId);
        return null;
    }


    private Invoice requireValidated(UUID userId, UUID invoiceId) {
        Invoice invoice = requireOwned(userId, invoiceId);
        if (invoice.getStatus() != InvoiceStatus.VALIDATED || invoice.getFactusNumber() == null) {
            throw new ApiException(409,
                    "La factura no está validada en Factus (status=" + invoice.getStatus() + ")");
        }
        return invoice;
    }

    private static String truncate(String value, int max) {
        if (value == null) return null;
        return value.length() <= max ? value : value.substring(0, max);
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

    @Transactional
    public void delete(UUID userId, UUID invoiceId) {
        Invoice invoice = requireOwned(userId, invoiceId);
        if (invoice.getStatus() == InvoiceStatus.VALIDATED) {
            throw new ApiException(409,
                    "No se puede eliminar una factura validada en Factus; emita una nota crédito");
        }

        // Si la factura llegó a Factus pero falló (status=ERROR), intentamos limpiar allí.
        // Si nunca llegó (status=PENDING sin factusNumber), no hay nada que borrar remotamente.
        if (invoice.getFactusNumber() != null || invoice.getStatus() == InvoiceStatus.ERROR) {
            try {
                // emitir nota credito en factus
            } catch (Exception e) {
                log.warn("No se pudo eliminar factura en Factus (referenceCode={}): {}",
                        invoice.getReferenceCode(), e.getMessage());
                // Continuamos con el delete local de todos modos.
            }
        }

        invoiceRepository.delete(invoice);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        if (user.getInvoiceCount() > 0) {
            user.setInvoiceCount(user.getInvoiceCount() - 1);
            userRepository.save(user);
        }

        log.info("Factura eliminada: id={}", invoiceId);
    }

    private Invoice requireOwned(UUID userId, UUID invoiceId) {
        Establishment establishment = establishmentService.getEntityByUserId(userId);
        return invoiceRepository.findByIdAndEstablishmentId(invoiceId, establishment.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Invoice", "id", invoiceId));
    }

    // ----- Validaciones cruzadas -----

    private void validatePayments(List<InvoicePaymentRequestDto> payments) {
        for (InvoicePaymentRequestDto p : payments) {
            // validar
            if (p.getPaymentForm().equals("2") && p.getDueDate() == null) {
                throw new ApiException(400,
                        "Pagos a crédito (paymentForm='2') requieren dueDate");
            }
        }
    }

    private void validateItemTaxes(List<InvoiceItemRequestDto> items) {
        for (int i = 0; i < items.size(); i++) {
            List<InvoiceItemTaxRequestDto> taxes = items.get(i).getTaxes();
            for (InvoiceItemTaxRequestDto tax : taxes) {
                boolean withholding = Boolean.TRUE.equals(tax.getIsWithholding());
                Set<String> allowed = withholding ? VALID_WITHHOLDING_CODES : VALID_TAX_CODES;
                if (!allowed.contains(tax.getTaxCode())) {
                    throw new ApiException(400,
                            "Item #" + (i + 1) + ": código '" + tax.getTaxCode()
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
            if (dto.getProductId() != null) {
                Product product = productRepository
                        .findByIdAndEstablishmentId(dto.getProductId(), establishmentId)
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Product", "id", dto.getProductId()));
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

    private void attachPayments(Invoice invoice, List<InvoicePaymentRequestDto> dtos) {
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
     *
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
}
