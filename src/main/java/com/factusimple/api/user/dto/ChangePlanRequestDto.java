package com.factusimple.api.user.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record ChangePlanRequestDto(
    @NotNull(message = "El ID del plan es requerido")
    UUID planId,

    String inviteCode
) {}
