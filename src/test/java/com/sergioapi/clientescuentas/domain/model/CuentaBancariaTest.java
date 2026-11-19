package com.sergioapi.clientescuentas.domain.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CuentaBancariaTest {

    private static final String DNI = "11111111A";

    @Test
    void shouldNormalizeTipoCuenta() {
        var cuenta = CuentaBancaria.crear(DNI, " premium ", BigDecimal.ZERO);

        assertThat(cuenta.tipoCuenta()).isEqualTo("PREMIUM");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void shouldRejectEmptyTipoCuenta(String tipoCuenta) {
        assertThatThrownBy(() -> CuentaBancaria.crear(DNI, tipoCuenta, BigDecimal.ZERO))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldRejectTooLongTipoCuenta() {
        assertThatThrownBy(() -> CuentaBancaria.crear(DNI, "A".repeat(51), BigDecimal.ZERO))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "0.100", "-500.25", "99999999999999999.99"})
    void shouldAcceptValidTotal(String total) {
        var cuenta = CuentaBancaria.crear(DNI, "NORMAL", new BigDecimal(total));

        assertThat(cuenta.total()).isEqualByComparingTo(new BigDecimal(total));
        assertThat(cuenta.total().scale()).isEqualTo(2);
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"10.123", "100000000000000000", "-100000000000000000"})
    void shouldRejectInvalidTotal(String total) {
        var importe = total == null ? null : new BigDecimal(total);

        assertThatThrownBy(() -> CuentaBancaria.crear(DNI, "NORMAL", importe))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldUpdateSaldo() {
        var cuenta = new CuentaBancaria(1L, DNI, "NORMAL", new BigDecimal("50000.00"));
        var nuevoTotal = new BigDecimal("75000.00");

        var actualizada = cuenta.actualizarSaldo(nuevoTotal);

        assertThat(actualizada).isEqualTo(new CuentaBancaria(1L, DNI, "NORMAL", nuevoTotal));
        assertThat(cuenta.total()).isEqualTo(new BigDecimal("50000.00"));
    }

    @Test
    void shouldRejectInvalidDniCliente() {
        assertThatThrownBy(() -> CuentaBancaria.crear("INVALID", "NORMAL", BigDecimal.ZERO))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldRejectTipoCuentaExceedingLimitAfterNormalization() {
        assertThatThrownBy(() ->
                CuentaBancaria.crear(
                        DNI, "ß".repeat(50), BigDecimal.ZERO
                ))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
