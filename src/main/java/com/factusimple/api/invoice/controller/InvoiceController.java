package com.factusimple.api.invoice.controller;

import com.factusimple.api.infrastructure.exception.ResourceNotFoundException;
import com.factusimple.api.invoice.dto.InvoiceRequestDto;
import com.factusimple.api.invoice.dto.InvoiceResponseDto;
import com.factusimple.api.invoice.entity.InvoiceStatus;
import com.factusimple.api.invoice.service.InvoiceService;
import com.factusimple.api.shared.dto.ApiResponseDto;
import com.factusimple.api.shared.dto.PageResponseDto;
import com.factusimple.api.user.entity.User;
import com.factusimple.api.user.repository.UserRepository;
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
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/v1/invoices")
@RequiredArgsConstructor
public class InvoiceController {

    private final InvoiceService invoiceService;
    private final UserRepository userRepository;

    @PostMapping
    public ResponseEntity<ApiResponseDto<InvoiceResponseDto>> create(
            @AuthenticationPrincipal UserDetails principal,
            @Valid @RequestBody InvoiceRequestDto requestDto) {
        User user = resolveUser(principal);
        InvoiceResponseDto dto = invoiceService.create(user.getId(), requestDto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDto.success("Factura creada", dto));
    }

    @GetMapping
    public ResponseEntity<ApiResponseDto<PageResponseDto<InvoiceResponseDto>>> list(
            @AuthenticationPrincipal UserDetails principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) InvoiceStatus status,
            @RequestParam(required = false) UUID customerId) {
        User user = resolveUser(principal);
        Pageable pageable = PageRequest.of(page, size);
        Page<InvoiceResponseDto> invoicesPage = invoiceService.list(user.getId(), status, customerId, pageable);

        PageResponseDto<InvoiceResponseDto> pageResponse = PageResponseDto.<InvoiceResponseDto>builder()
                .content(invoicesPage.getContent())
                .pageNumber(invoicesPage.getNumber())
                .pageSize(invoicesPage.getSize())
                .totalElements(invoicesPage.getTotalElements())
                .totalPages(invoicesPage.getTotalPages())
                .isLast(invoicesPage.isLast())
                .build();

        return ResponseEntity.ok(ApiResponseDto.success("Facturas obtenidas", pageResponse));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDto<InvoiceResponseDto>> get(
            @AuthenticationPrincipal UserDetails principal,
            @PathVariable UUID id) {
        User user = resolveUser(principal);
        InvoiceResponseDto dto = invoiceService.get(user.getId(), id);
        return ResponseEntity.ok(ApiResponseDto.success("Factura obtenida", dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseDto<Void>> delete(
            @AuthenticationPrincipal UserDetails principal,
            @PathVariable UUID id) {
        User user = resolveUser(principal);
        invoiceService.delete(user.getId(), id);
        return ResponseEntity.ok(ApiResponseDto.success("Factura eliminada", null));
    }

    @PostMapping("/{id}/sync")
    public ResponseEntity<ApiResponseDto<InvoiceResponseDto>> sync(
            @AuthenticationPrincipal UserDetails principal,
            @PathVariable UUID id) {
        User user = resolveUser(principal);
        InvoiceResponseDto dto = invoiceService.syncWithFactus(user.getId(), id);
        return ResponseEntity.ok(ApiResponseDto.success("Sync con Factus ejecutado", dto));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<ApiResponseDto<InvoiceResponseDto>> cancel(
            @AuthenticationPrincipal UserDetails principal,
            @PathVariable UUID id) {
        User user = resolveUser(principal);
        InvoiceResponseDto dto = invoiceService.cancel(user.getId(), id);
        return ResponseEntity.ok(ApiResponseDto.success("Factura cancelada exitosamente", dto));
    }

    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> downloadPdf(
            @AuthenticationPrincipal UserDetails principal,
            @PathVariable UUID id) {
        User user = resolveUser(principal);
        byte[] bytes = invoiceService.downloadPdf(user.getId(), id);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(
                org.springframework.http.ContentDisposition.attachment()
                        .filename("invoice-" + id + ".pdf").build());
        return new ResponseEntity<>(bytes, headers, HttpStatus.OK);
    }

    @GetMapping("/{id}/xml")
    public ResponseEntity<byte[]> downloadXml(
            @AuthenticationPrincipal UserDetails principal,
            @PathVariable UUID id) {
        User user = resolveUser(principal);
        byte[] bytes = invoiceService.downloadXml(user.getId(), id);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_XML);
        headers.setContentDisposition(
                org.springframework.http.ContentDisposition.attachment()
                        .filename("invoice-" + id + ".xml").build());
        return new ResponseEntity<>(bytes, headers, HttpStatus.OK);
    }

    private User resolveUser(UserDetails principal) {
        return userRepository.findByEmail(principal.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User", "email", principal.getUsername()));
    }
}
