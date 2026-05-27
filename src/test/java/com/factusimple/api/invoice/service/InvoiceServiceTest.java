package com.factusimple.api.invoice.service;

import com.factusimple.api.customer.repository.CustomerRepository;
import com.factusimple.api.establishment.service.EstablishmentService;
import com.factusimple.api.infrastructure.exception.*;
import com.factusimple.api.infrastructure.factus.client.FactusBillsClient;
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
import com.factusimple.api.user.repository.UserRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class InvoiceServiceTest {

    @Mock
    private InvoiceRepository invoiceRepository;

    @Mock
    private InvoiceMapper invoiceMapper;

    @Mock
    private EstablishmentService establishmentService;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private FactusBillsClient factusBillsClient;

    @InjectMocks
    private InvoiceService invoiceService;

    @Nested
    @DisplayName("applyTotals()")
    class ApplyTotalsTests {

        @Test
        @DisplayName("Should calculate totals correctly with single item and IVA 19%")
        void testSingleItemWithIva19Percent() {
            Invoice invoice = createInvoice();
            InvoiceItem item = createItem(
                    new BigDecimal("10"),
                    new BigDecimal("10"),
                    new BigDecimal("0")
            );
            InvoiceItemTax tax = createTax(new BigDecimal("19"), false);
            item.getTaxes().add(tax);
            invoice.getItems().add(item);

            ReflectionTestUtils.invokeMethod(invoiceService, "applyTotals", invoice);

            assertEquals(new BigDecimal("100.00"), invoice.getSubtotal());
            assertEquals(new BigDecimal("19.00"), invoice.getTotalTaxes());
            assertEquals(new BigDecimal("119.00"), invoice.getTotal());
        }

        @Test
        @DisplayName("Should apply discount before calculating tax")
        void testDiscountBeforeTax() {
            Invoice invoice = createInvoice();
            InvoiceItem item = createItem(
                    new BigDecimal("10"),
                    new BigDecimal("10"),
                    new BigDecimal("10")
            );
            InvoiceItemTax tax = createTax(new BigDecimal("19"), false);
            item.getTaxes().add(tax);
            invoice.getItems().add(item);

            ReflectionTestUtils.invokeMethod(invoiceService, "applyTotals", invoice);

            assertEquals(new BigDecimal("100.00"), invoice.getSubtotal());
            assertEquals(new BigDecimal("10.00"), invoice.getTotalDiscounts());
            BigDecimal expectedTax = new BigDecimal("90.00").multiply(new BigDecimal("19"))
                    .divide(new BigDecimal("100"), 2, java.math.RoundingMode.HALF_UP);
            assertEquals(expectedTax, invoice.getTotalTaxes());
        }

        @Test
        @DisplayName("Should accumulate totals for multiple items")
        void testMultipleItems() {
            Invoice invoice = createInvoice();

            InvoiceItem item1 = createItem(
                    new BigDecimal("10"),
                    new BigDecimal("10"),
                    new BigDecimal("0")
            );
            InvoiceItemTax tax1 = createTax(new BigDecimal("19"), false);
            item1.getTaxes().add(tax1);
            invoice.getItems().add(item1);

            InvoiceItem item2 = createItem(
                    new BigDecimal("5"),
                    new BigDecimal("10"),
                    new BigDecimal("0")
            );
            InvoiceItemTax tax2 = createTax(new BigDecimal("19"), false);
            item2.getTaxes().add(tax2);
            invoice.getItems().add(item2);

            ReflectionTestUtils.invokeMethod(invoiceService, "applyTotals", invoice);

            assertEquals(new BigDecimal("150.00"), invoice.getSubtotal());
            assertEquals(new BigDecimal("28.50"), invoice.getTotalTaxes());
            assertEquals(new BigDecimal("178.50"), invoice.getTotal());
        }

        @Test
        @DisplayName("Should exclude withholding taxes from total")
        void testWithholdingTaxNotIncludedInTotal() {
            Invoice invoice = createInvoice();
            InvoiceItem item = createItem(
                    new BigDecimal("10"),
                    new BigDecimal("10"),
                    new BigDecimal("0")
            );
            InvoiceItemTax tax = createTax(new BigDecimal("10"), true);
            item.getTaxes().add(tax);
            invoice.getItems().add(item);

            ReflectionTestUtils.invokeMethod(invoiceService, "applyTotals", invoice);

            assertEquals(new BigDecimal("100.00"), invoice.getSubtotal());
            assertEquals(new BigDecimal("0.00"), invoice.getTotalTaxes());
            assertEquals(new BigDecimal("100.00"), invoice.getTotal());
        }

        @Test
        @DisplayName("Should result in zero total for empty invoice")
        void testEmptyInvoice() {
            Invoice invoice = createInvoice();

            ReflectionTestUtils.invokeMethod(invoiceService, "applyTotals", invoice);

            assertEquals(new BigDecimal("0.00"), invoice.getSubtotal());
            assertEquals(new BigDecimal("0.00"), invoice.getTotalTaxes());
            assertEquals(new BigDecimal("0.00"), invoice.getTotal());
        }

        @Test
        @DisplayName("Should apply surcharges and allowances correctly")
        void testSurchargesAndAllowances() {
            Invoice invoice = createInvoice();
            InvoiceItem item = createItem(
                    new BigDecimal("10"),
                    new BigDecimal("10"),
                    new BigDecimal("0")
            );
            InvoiceItemTax tax = createTax(new BigDecimal("19"), false);
            item.getTaxes().add(tax);
            invoice.getItems().add(item);

            AllowanceCharge allowance = new AllowanceCharge();
            allowance.setAmount(new BigDecimal("10"));
            allowance.setIsSurcharge(false);
            invoice.getAllowanceCharges().add(allowance);

            AllowanceCharge surcharge = new AllowanceCharge();
            surcharge.setAmount(new BigDecimal("5"));
            surcharge.setIsSurcharge(true);
            invoice.getAllowanceCharges().add(surcharge);

            ReflectionTestUtils.invokeMethod(invoiceService, "applyTotals", invoice);

            assertEquals(new BigDecimal("100.00"), invoice.getSubtotal());
            assertEquals(new BigDecimal("10.00"), invoice.getTotalDiscounts());
            assertEquals(new BigDecimal("19.00"), invoice.getTotalTaxes());
            assertEquals(new BigDecimal("114.00"), invoice.getTotal());
        }

        @Test
        @DisplayName("Should apply prepayments to total")
        void testPrepaymentsDeductedFromTotal() {
            Invoice invoice = createInvoice();
            InvoiceItem item = createItem(
                    new BigDecimal("10"),
                    new BigDecimal("10"),
                    new BigDecimal("0")
            );
            InvoiceItemTax tax = createTax(new BigDecimal("19"), false);
            item.getTaxes().add(tax);
            invoice.getItems().add(item);

            InvoicePrepayment prepayment = new InvoicePrepayment();
            prepayment.setAmount(new BigDecimal("30"));
            invoice.getPrepayments().add(prepayment);

            ReflectionTestUtils.invokeMethod(invoiceService, "applyTotals", invoice);

            assertEquals(new BigDecimal("100.00"), invoice.getSubtotal());
            assertEquals(new BigDecimal("19.00"), invoice.getTotalTaxes());
            assertEquals(new BigDecimal("89.00"), invoice.getTotal());
        }

        @Test
        @DisplayName("Should apply cash rounding to total")
        void testCashRounding() {
            Invoice invoice = createInvoice();
            invoice.setCashRounding(new BigDecimal("0.50"));

            InvoiceItem item = createItem(
                    new BigDecimal("10"),
                    new BigDecimal("10"),
                    new BigDecimal("0")
            );
            InvoiceItemTax tax = createTax(new BigDecimal("19"), false);
            item.getTaxes().add(tax);
            invoice.getItems().add(item);

            ReflectionTestUtils.invokeMethod(invoiceService, "applyTotals", invoice);

            assertEquals(new BigDecimal("119.50"), invoice.getTotal());
        }
    }

    @Nested
    @DisplayName("validatePayments()")
    class ValidatePaymentsTests {

        @Test
        @DisplayName("Should validate credit payment requires due date")
        void testCreditPaymentRequiresDueDate() {
            PaymentRequestDto payment = new PaymentRequestDto(
                "2", "05", null, BigDecimal.ZERO, null
            );

            assertThrows(BadRequestException.class, () -> {
                ReflectionTestUtils.invokeMethod(invoiceService, "validatePayments",List.of(payment));
            });
        }

        @Test
        @DisplayName("Should accept credit payment with due date")
        void testCreditPaymentWithDueDate() {
            PaymentRequestDto payment = new PaymentRequestDto(
                "2", "05", null, BigDecimal.ZERO, LocalDate.of(2026, 6, 30)
            );

            assertDoesNotThrow(() -> {
                ReflectionTestUtils.invokeMethod(invoiceService, "validatePayments",List.of(payment));
            });
        }

        @Test
        @DisplayName("Should accept cash payment without due date")
        void testCashPaymentWithoutDueDate() {
            PaymentRequestDto payment = new PaymentRequestDto(
                "1", "10", null, BigDecimal.ZERO, null
            );

            assertDoesNotThrow(() -> {
                ReflectionTestUtils.invokeMethod(invoiceService, "validatePayments",List.of(payment));
            });
        }

        @Test
        @DisplayName("Should accept empty payment list")
        void testEmptyPaymentList() {
            assertDoesNotThrow(() -> {
                ReflectionTestUtils.invokeMethod(invoiceService, "validatePayments",List.of());
            });
        }
    }

    @Nested
    @DisplayName("validateItemTaxes()")
    class ValidateItemTaxesTests {

        @Test
        @DisplayName("Should accept valid IVA tax code")
        void testValidIvaTaxCode() {
            InvoiceItemRequestDto item = createItemRequest();
            ItemTaxRequestDto tax = new ItemTaxRequestDto(
                TaxCode.IVA.getCode(), new BigDecimal("19"), false
            );
            item.taxes().add(tax);

            assertDoesNotThrow(() -> {
                ReflectionTestUtils.invokeMethod(invoiceService, "validateItemTaxes",List.of(item));
            });
        }

        @Test
        @DisplayName("Should reject invalid tax code")
        void testInvalidTaxCode() {
            InvoiceItemRequestDto item = createItemRequest();
            ItemTaxRequestDto tax = new ItemTaxRequestDto(
                "INVALID", BigDecimal.ZERO, false
            );
            item.taxes().add(tax);

            assertThrows(BadRequestException.class, () -> {
                ReflectionTestUtils.invokeMethod(invoiceService, "validateItemTaxes",List.of(item));
            });
        }

        @Test
        @DisplayName("Should accept valid withholding tax code")
        void testValidWithholdingTaxCode() {
            InvoiceItemRequestDto item = createItemRequest();
            ItemTaxRequestDto tax = new ItemTaxRequestDto(
                WithholdingTaxCode.RETE_IVA.getCode(), new BigDecimal("1"), true
            );
            item.taxes().add(tax);

            assertDoesNotThrow(() -> {
                ReflectionTestUtils.invokeMethod(invoiceService, "validateItemTaxes",List.of(item));
            });
        }

        @Test
        @DisplayName("Should reject invalid withholding tax code")
        void testInvalidWithholdingTaxCode() {
            InvoiceItemRequestDto item = createItemRequest();
            ItemTaxRequestDto tax = new ItemTaxRequestDto(
                TaxCode.IVA.getCode(), BigDecimal.ZERO, true
            );
            item.taxes().add(tax);

            assertThrows(BadRequestException.class, () -> {
                ReflectionTestUtils.invokeMethod(invoiceService, "validateItemTaxes",List.of(item));
            });
        }

        @Test
        @DisplayName("Should accept empty tax list")
        void testEmptyTaxList() {
            InvoiceItemRequestDto item = new InvoiceItemRequestDto(
                null, null, "Test Item", new BigDecimal("1"), new BigDecimal("100"),
                null, null, null, null, List.of()
            );

            assertDoesNotThrow(() -> {
                ReflectionTestUtils.invokeMethod(invoiceService, "validateItemTaxes",List.of(item));
            });
        }

        @Test
        @DisplayName("Should accept multiple valid taxes on single item")
        void testMultipleValidTaxes() {
            InvoiceItemRequestDto item = createItemRequest();

            ItemTaxRequestDto tax1 = new ItemTaxRequestDto(
                TaxCode.IVA.getCode(), BigDecimal.ZERO, false
            );
            item.taxes().add(tax1);

            ItemTaxRequestDto tax2 = new ItemTaxRequestDto(
                WithholdingTaxCode.RETE_IVA.getCode(), BigDecimal.ZERO, true
            );
            item.taxes().add(tax2);

            assertDoesNotThrow(() -> {
                ReflectionTestUtils.invokeMethod(invoiceService, "validateItemTaxes",List.of(item));
            });
        }
    }

    @Nested
    @DisplayName("validateStockAvailability()")
    class ValidateStockAvailabilityTests {

        @Test
        @DisplayName("Should pass when stock is sufficient")
        void testSufficientStock() {
            InvoiceItem item = createInvoiceItem();
            Product product = new Product();
            product.setStock(new BigDecimal("100"));
            product.setSku("TEST-SKU");
            item.setProduct(product);
            item.setQuantity(new BigDecimal("50"));

            assertDoesNotThrow(() -> {
                ReflectionTestUtils.invokeMethod(invoiceService, "validateStockAvailability",List.of(item));
            });
        }

        @Test
        @DisplayName("Should fail when stock is insufficient")
        void testInsufficientStock() {
            InvoiceItem item = createInvoiceItem();
            Product product = new Product();
            product.setStock(new BigDecimal("30"));
            product.setSku("TEST-SKU");
            item.setProduct(product);
            item.setQuantity(new BigDecimal("50"));

            assertThrows(UnprocessableEntityException.class, () -> {
                ReflectionTestUtils.invokeMethod(invoiceService, "validateStockAvailability",List.of(item));
            });
        }

        @Test
        @DisplayName("Should pass when stock equals quantity")
        void testStockEqualsQuantity() {
            InvoiceItem item = createInvoiceItem();
            Product product = new Product();
            product.setStock(new BigDecimal("50"));
            product.setSku("TEST-SKU");
            item.setProduct(product);
            item.setQuantity(new BigDecimal("50"));

            assertDoesNotThrow(() -> {
                ReflectionTestUtils.invokeMethod(invoiceService, "validateStockAvailability",List.of(item));
            });
        }

        @Test
        @DisplayName("Should skip validation when item has no product")
        void testNoProductSkipsValidation() {
            InvoiceItem item = createInvoiceItem();
            item.setProduct(null);
            item.setQuantity(new BigDecimal("999"));

            assertDoesNotThrow(() -> {
                ReflectionTestUtils.invokeMethod(invoiceService, "validateStockAvailability",List.of(item));
            });
        }

        @Test
        @DisplayName("Should skip validation when product has no stock")
        void testNoStockSkipsValidation() {
            InvoiceItem item = createInvoiceItem();
            Product product = new Product();
            product.setStock(null);
            product.setSku("TEST-SKU");
            item.setProduct(product);
            item.setQuantity(new BigDecimal("999"));

            assertDoesNotThrow(() -> {
                ReflectionTestUtils.invokeMethod(invoiceService, "validateStockAvailability",List.of(item));
            });
        }

        @Test
        @DisplayName("Should validate multiple items with mixed stock scenarios")
        void testMultipleItemsWithMixedStock() {
            InvoiceItem item1 = createInvoiceItem();
            Product product1 = new Product();
            product1.setStock(new BigDecimal("100"));
            product1.setSku("SKU-1");
            item1.setProduct(product1);
            item1.setQuantity(new BigDecimal("50"));

            InvoiceItem item2 = createInvoiceItem();
            Product product2 = new Product();
            product2.setStock(new BigDecimal("30"));
            product2.setSku("SKU-2");
            item2.setProduct(product2);
            item2.setQuantity(new BigDecimal("30"));

            assertDoesNotThrow(() -> {
                ReflectionTestUtils.invokeMethod(invoiceService, "validateStockAvailability",List.of(item1, item2));
            });
        }

        @Test
        @DisplayName("Should fail on first item with insufficient stock in batch")
        void testFirstItemFailsInBatch() {
            InvoiceItem item1 = createInvoiceItem();
            Product product1 = new Product();
            product1.setStock(new BigDecimal("30"));
            product1.setSku("SKU-1");
            item1.setProduct(product1);
            item1.setQuantity(new BigDecimal("50"));

            InvoiceItem item2 = createInvoiceItem();
            Product product2 = new Product();
            product2.setStock(new BigDecimal("100"));
            product2.setSku("SKU-2");
            item2.setProduct(product2);
            item2.setQuantity(new BigDecimal("50"));

            assertThrows(UnprocessableEntityException.class, () -> {
                ReflectionTestUtils.invokeMethod(invoiceService, "validateStockAvailability",List.of(item1, item2));
            });
        }
    }

    // ===== Helper methods =====

    private Invoice createInvoice() {
        Invoice invoice = new Invoice();
        invoice.setItems(new ArrayList<>());
        invoice.setPayments(new ArrayList<>());
        invoice.setPrepayments(new ArrayList<>());
        invoice.setAllowanceCharges(new ArrayList<>());
        return invoice;
    }

    private InvoiceItem createItem(BigDecimal unitPrice, BigDecimal quantity, BigDecimal discountRate) {
        InvoiceItem item = new InvoiceItem();
        item.setUnitPrice(unitPrice);
        item.setQuantity(quantity);
        item.setDiscountRate(discountRate);
        item.setTaxes(new ArrayList<>());
        return item;
    }

    private InvoiceItem createInvoiceItem() {
        InvoiceItem item = new InvoiceItem();
        item.setTaxes(new ArrayList<>());
        return item;
    }

    private InvoiceItemTax createTax(BigDecimal taxRate, boolean isWithholding) {
        InvoiceItemTax tax = new InvoiceItemTax();
        tax.setTaxRate(taxRate);
        tax.setIsWithholding(isWithholding);
        tax.setTaxCode(isWithholding ? "01" : "19");
        return tax;
    }

    private InvoiceItemRequestDto createItemRequest() {
        return new InvoiceItemRequestDto(
            null,                      // productId
            null,                      // codeReference
            "Test Item",               // name
            new BigDecimal("1"),       // quantity
            new BigDecimal("100"),     // unitPrice
            null,                      // discountRate
            null,                      // unitMeasureCode
            null,                      // standardCode
            null,                      // note
            new ArrayList<>()          // taxes (mutable list for adding items in tests)
        );
    }
}
