package com.sergioapi.clientescuentas.application.model;

import com.sergioapi.clientescuentas.domain.model.Cliente;
import com.sergioapi.clientescuentas.domain.model.CuentaBancaria;

import java.util.List;

public record ClienteConCuentas(
        Cliente cliente,
        List<CuentaBancaria> cuentas
) {
    public ClienteConCuentas {
        cuentas = List.copyOf(cuentas);
    }
}