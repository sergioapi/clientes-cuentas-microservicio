package com.sergioapi.clientescuentas.infrastructure.rest.dto;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record UpdateSaldoRequest(
        @NotNull BigDecimal total
) {
}
