package com.factusimple.api.infrastructure.factus.dto;

import java.util.List;

public record FactusPaginatedResponse<T>(List<T> data) {
}