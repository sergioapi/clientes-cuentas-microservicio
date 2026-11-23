package com.sergioapi.clientescuentas.infrastructure.rest.dto;

import java.math.BigDecimal;

public record CuentaBancariaResponse(
        Long id,
        String dniCliente,
        String tipoCuenta,
        BigDecimal total
) {
}
