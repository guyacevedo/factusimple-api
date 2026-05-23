package com.factusimple.api.invoice.controller;

import com.factusimple.api.infrastructure.filter.CustomUserDetails;
import com.factusimple.api.invoice.dto.InvoiceRequestDto;
import com.factusimple.api.invoice.dto.InvoiceResponseDto;
import com.factusimple.api.invoice.entity.InvoiceStatus;
import com.factusimple.api.invoice.service.InvoiceService;
import com.factusimple.api.shared.dto.ApiResponseDto;
import com.factusimple.api.shared.dto.PageResponseDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/v1/invoices")
@RequiredArgsConstructor
public class InvoiceController {

    private final InvoiceService invoiceService;

    @PostMapping
    public ResponseEntity<ApiResponseDto<InvoiceResponseDto>> create(
            @AuthenticationPrincipal CustomUserDetails principal,
            @Valid @RequestBody InvoiceRequestDto requestDto) {
        InvoiceResponseDto dto = invoiceService.create(principal.getUserId(), requestDto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDto.success("Factura creada", dto));
    }

    @GetMapping
    public ResponseEntity<ApiResponseDto<PageResponseDto<InvoiceResponseDto>>> list(
            @AuthenticationPrincipal CustomUserDetails principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) InvoiceStatus status,
            @RequestParam(required = false) UUID customerId) {
        Pageable pageable = PageRequest.of(page, size);
        Page<InvoiceResponseDto> invoicesPage = invoiceService.list(principal.getUserId(), status, customerId, pageable);
        return ResponseEntity.ok(ApiResponseDto.success("Facturas obtenidas", PageResponseDto.from(invoicesPage)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDto<InvoiceResponseDto>> get(
            @AuthenticationPrincipal CustomUserDetails principal,
            @PathVariable UUID id) {
        InvoiceResponseDto dto = invoiceService.get(principal.getUserId(), id);
        return ResponseEntity.ok(ApiResponseDto.success("Factura obtenida", dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseDto<Void>> delete(
            @AuthenticationPrincipal CustomUserDetails principal,
            @PathVariable UUID id) {
        invoiceService.delete(principal.getUserId(), id);
        return ResponseEntity.ok(ApiResponseDto.success("Factura eliminada", null));
    }

    @PostMapping("/{id}/sync")
    public ResponseEntity<ApiResponseDto<InvoiceResponseDto>> sync(
            @AuthenticationPrincipal CustomUserDetails principal,
            @PathVariable UUID id) {
        InvoiceResponseDto dto = invoiceService.syncWithFactus(principal.getUserId(), id);
        return ResponseEntity.ok(ApiResponseDto.success("Sync con Factus ejecutado", dto));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<ApiResponseDto<InvoiceResponseDto>> cancel(
            @AuthenticationPrincipal CustomUserDetails principal,
            @PathVariable UUID id) {
        InvoiceResponseDto dto = invoiceService.cancel(principal.getUserId(), id);
        return ResponseEntity.ok(ApiResponseDto.success("Factura cancelada exitosamente", dto));
    }

    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> downloadPdf(
            @AuthenticationPrincipal CustomUserDetails principal,
            @PathVariable UUID id) {
        byte[] bytes = invoiceService.downloadPdf(principal.getUserId(), id);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(
                org.springframework.http.ContentDisposition.attachment()
                        .filename("invoice-" + id + ".pdf").build());
        return new ResponseEntity<>(bytes, headers, HttpStatus.OK);
    }

    @GetMapping("/{id}/xml")
    public ResponseEntity<byte[]> downloadXml(
            @AuthenticationPrincipal CustomUserDetails principal,
            @PathVariable UUID id) {
        byte[] bytes = invoiceService.downloadXml(principal.getUserId(), id);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_XML);
        headers.setContentDisposition(
                org.springframework.http.ContentDisposition.attachment()
                        .filename("invoice-" + id + ".xml").build());
        return new ResponseEntity<>(bytes, headers, HttpStatus.OK);
    }
}
