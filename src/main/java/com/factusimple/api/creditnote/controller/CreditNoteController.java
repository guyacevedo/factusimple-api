package com.factusimple.api.creditnote.controller;

import com.factusimple.api.creditnote.dto.CreditNoteRequestDto;
import com.factusimple.api.creditnote.dto.CreditNoteResponseDto;
import com.factusimple.api.creditnote.entity.CreditNoteStatus;
import com.factusimple.api.creditnote.service.CreditNoteService;
import com.factusimple.api.infrastructure.exception.ResourceNotFoundException;
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
@RequestMapping("/v1/credit-notes")
@RequiredArgsConstructor
public class CreditNoteController {

    private final CreditNoteService creditNoteService;
    private final UserRepository userRepository;

    @PostMapping
    public ResponseEntity<ApiResponseDto<CreditNoteResponseDto>> create(
            @AuthenticationPrincipal UserDetails principal,
            @Valid @RequestBody CreditNoteRequestDto requestDto) {
        User user = resolveUser(principal);
        CreditNoteResponseDto dto = creditNoteService.create(user.getId(), requestDto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDto.success("Nota de crédito creada", dto));
    }

    @GetMapping
    public ResponseEntity<ApiResponseDto<PageResponseDto<CreditNoteResponseDto>>> list(
            @AuthenticationPrincipal UserDetails principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) CreditNoteStatus status) {
        User user = resolveUser(principal);
        Pageable pageable = PageRequest.of(page, size);
        Page<CreditNoteResponseDto> creditNotesPage = creditNoteService.list(user.getId(), status, pageable);

        PageResponseDto<CreditNoteResponseDto> pageResponse = PageResponseDto.<CreditNoteResponseDto>builder()
                .content(creditNotesPage.getContent())
                .pageNumber(creditNotesPage.getNumber())
                .pageSize(creditNotesPage.getSize())
                .totalElements(creditNotesPage.getTotalElements())
                .totalPages(creditNotesPage.getTotalPages())
                .isLast(creditNotesPage.isLast())
                .build();

        return ResponseEntity.ok(ApiResponseDto.success("Notas de crédito obtenidas", pageResponse));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDto<CreditNoteResponseDto>> get(
            @AuthenticationPrincipal UserDetails principal,
            @PathVariable UUID id) {
        User user = resolveUser(principal);
        CreditNoteResponseDto dto = creditNoteService.get(user.getId(), id);
        return ResponseEntity.ok(ApiResponseDto.success("Nota de crédito obtenida", dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseDto<Void>> delete(
            @AuthenticationPrincipal UserDetails principal,
            @PathVariable UUID id) {
        User user = resolveUser(principal);
        creditNoteService.delete(user.getId(), id);
        return ResponseEntity.ok(ApiResponseDto.success("Nota de crédito eliminada", null));
    }

    @PostMapping("/{id}/sync")
    public ResponseEntity<ApiResponseDto<CreditNoteResponseDto>> syncWithFactus(
            @AuthenticationPrincipal UserDetails principal,
            @PathVariable UUID id) {
        User user = resolveUser(principal);
        CreditNoteResponseDto dto = creditNoteService.syncWithFactus(user.getId(), id);
        return ResponseEntity.ok(ApiResponseDto.success("Sincronización con Factus ejecutada", dto));
    }

    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> downloadPdf(
            @AuthenticationPrincipal UserDetails principal,
            @PathVariable UUID id) {
        User user = resolveUser(principal);
        byte[] bytes = creditNoteService.downloadPdf(user.getId(), id);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(
                org.springframework.http.ContentDisposition.attachment()
                        .filename("credit-note-" + id + ".pdf").build());
        return new ResponseEntity<>(bytes, headers, HttpStatus.OK);
    }

    @GetMapping("/{id}/xml")
    public ResponseEntity<byte[]> downloadXml(
            @AuthenticationPrincipal UserDetails principal,
            @PathVariable UUID id) {
        User user = resolveUser(principal);
        byte[] bytes = creditNoteService.downloadXml(user.getId(), id);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_XML);
        headers.setContentDisposition(
                org.springframework.http.ContentDisposition.attachment()
                        .filename("credit-note-" + id + ".xml").build());
        return new ResponseEntity<>(bytes, headers, HttpStatus.OK);
    }

    private User resolveUser(UserDetails principal) {
        return userRepository.findByEmail(principal.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User", "email", principal.getUsername()));
    }
}
