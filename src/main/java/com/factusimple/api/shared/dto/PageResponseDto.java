package com.factusimple.api.shared.dto;

import org.springframework.data.domain.Page;

import java.util.List;

public record PageResponseDto<T>(
    List<T> content,
    Integer pageNumber,
    Integer pageSize,
    Long totalElements,
    Integer totalPages,
    Boolean isLast
) {
    public static <T> PageResponseDto<T> from(Page<T> page) {
        return new PageResponseDto<>(
            page.getContent(),
            page.getNumber(),
            page.getSize(),
            page.getTotalElements(),
            page.getTotalPages(),
            page.isLast()
        );
    }
}
