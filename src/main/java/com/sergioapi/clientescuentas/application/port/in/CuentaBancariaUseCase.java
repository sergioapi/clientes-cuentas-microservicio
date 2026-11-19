package com.sergioapi.clientescuentas.application.port.in;

import com.sergioapi.clientescuentas.domain.model.CuentaBancaria;

import java.math.BigDecimal;

public interface CuentaBancariaUseCase {
    CuentaBancaria createCuenta(
            String dniCliente, String tipoCuenta, BigDecimal total
    );

    CuentaBancaria updateCuentaSaldo(Long idCuenta, BigDecimal total);
}
