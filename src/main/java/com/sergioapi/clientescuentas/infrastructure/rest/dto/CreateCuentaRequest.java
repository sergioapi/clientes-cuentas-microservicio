package com.sergioapi.clientescuentas.infrastructure.rest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CreateCuentaRequest(

        @NotBlank
        @Pattern(regexp = "^[0-9]{8}[A-Z]$")
        String dniCliente,

        @NotBlank
        @Size(max = 50)
        String tipoCuenta,

        @NotNull
        BigDecimal total

) {
}
