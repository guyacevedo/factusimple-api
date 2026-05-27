package com.factusimple.api.creditnote.controller;

import com.factusimple.api.creditnote.dto.CreditNoteRequestDto;
import com.factusimple.api.creditnote.dto.CreditNoteResponseDto;
import com.factusimple.api.creditnote.entity.CreditNoteStatus;
import com.factusimple.api.creditnote.service.CreditNoteService;
import com.factusimple.api.infrastructure.filter.CustomUserDetails;
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
@RequestMapping("/v1/credit-notes")
@RequiredArgsConstructor
public class CreditNoteController {

    private final CreditNoteService creditNoteService;

    @PostMapping
    public ResponseEntity<ApiResponseDto<CreditNoteResponseDto>> create(
            @AuthenticationPrincipal CustomUserDetails principal,
            @Valid @RequestBody CreditNoteRequestDto requestDto) {
        CreditNoteResponseDto dto = creditNoteService.create(principal.getUserId(), requestDto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDto.success("Nota de crédito creada", dto));
    }

    @GetMapping
    public ResponseEntity<ApiResponseDto<PageResponseDto<CreditNoteResponseDto>>> list(
            @AuthenticationPrincipal CustomUserDetails principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) CreditNoteStatus status) {
        Pageable pageable = PageRequest.of(page, Math.min(size, 100));
        Page<CreditNoteResponseDto> creditNotesPage = creditNoteService.list(principal.getUserId(), status, pageable);
        return ResponseEntity.ok(ApiResponseDto.success("Notas de crédito obtenidas", PageResponseDto.from(creditNotesPage)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDto<CreditNoteResponseDto>> get(
            @AuthenticationPrincipal CustomUserDetails principal,
            @PathVariable UUID id) {
        CreditNoteResponseDto dto = creditNoteService.get(principal.getUserId(), id);
        return ResponseEntity.ok(ApiResponseDto.success("Nota de crédito obtenida", dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseDto<Void>> delete(
            @AuthenticationPrincipal CustomUserDetails principal,
            @PathVariable UUID id) {
        creditNoteService.delete(principal.getUserId(), id);
        return ResponseEntity.ok(ApiResponseDto.success("Nota de crédito eliminada", null));
    }

    @PostMapping("/{id}/sync")
    public ResponseEntity<ApiResponseDto<CreditNoteResponseDto>> syncWithFactus(
            @AuthenticationPrincipal CustomUserDetails principal,
            @PathVariable UUID id) {
        CreditNoteResponseDto dto = creditNoteService.syncWithFactus(principal.getUserId(), id);
        return ResponseEntity.ok(ApiResponseDto.success("Sincronización con Factus ejecutada", dto));
    }

    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> downloadPdf(
            @AuthenticationPrincipal CustomUserDetails principal,
            @PathVariable UUID id) {
        byte[] bytes = creditNoteService.downloadPdf(principal.getUserId(), id);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(
                org.springframework.http.ContentDisposition.attachment()
                        .filename("credit-note-" + id + ".pdf").build());
        return new ResponseEntity<>(bytes, headers, HttpStatus.OK);
    }

    @GetMapping("/{id}/xml")
    public ResponseEntity<byte[]> downloadXml(
            @AuthenticationPrincipal CustomUserDetails principal,
            @PathVariable UUID id) {
        byte[] bytes = creditNoteService.downloadXml(principal.getUserId(), id);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_XML);
        headers.setContentDisposition(
                org.springframework.http.ContentDisposition.attachment()
                        .filename("credit-note-" + id + ".xml").build());
        return new ResponseEntity<>(bytes, headers, HttpStatus.OK);
    }
}
