package com.sergioapi.clientescuentas.domain.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;

public record CuentaBancaria(
        Long id,
        String dniCliente,
        String tipoCuenta,
        BigDecimal total
) {
    private static final int MAX_TIPO_LENGTH = 50;
    private static final BigDecimal MAX_ABSOLUTE_TOTAL =
            new BigDecimal("100000000000000000");

    public CuentaBancaria {
        Cliente.validateDni(dniCliente);

        if (tipoCuenta == null || tipoCuenta.isBlank()) {
            throw new IllegalArgumentException("El tipo de cuenta no puede estar vacío");
        }

        tipoCuenta = tipoCuenta.strip().toUpperCase(Locale.ROOT);
        if (tipoCuenta.length() > MAX_TIPO_LENGTH) {
            throw new IllegalArgumentException("El tipo de cuenta no puede superar los 50 caracteres");
        }

        if (total == null
                || total.abs().compareTo(MAX_ABSOLUTE_TOTAL) >= 0
                || total.stripTrailingZeros().scale() > 2) {
            throw new IllegalArgumentException(
                    "El saldo debe tener hasta 17 dígitos enteros y 2 decimales"
            );
        }

        total = total.setScale(2, RoundingMode.UNNECESSARY);
    }

    public static CuentaBancaria crear(String dniCliente, String tipoCuenta, BigDecimal total) {
        return new CuentaBancaria(null, dniCliente, tipoCuenta, total);
    }

    public CuentaBancaria actualizarSaldo(BigDecimal nuevoTotal) {
        return new CuentaBancaria(id, dniCliente, tipoCuenta, nuevoTotal);
    }
}
