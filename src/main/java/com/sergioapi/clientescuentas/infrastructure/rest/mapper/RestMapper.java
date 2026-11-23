package com.sergioapi.clientescuentas.infrastructure.rest.mapper;

import com.sergioapi.clientescuentas.application.model.ClienteConCuentas;
import com.sergioapi.clientescuentas.domain.model.Cliente;
import com.sergioapi.clientescuentas.domain.model.CuentaBancaria;
import com.sergioapi.clientescuentas.infrastructure.rest.dto.ClienteConCuentasResponse;
import com.sergioapi.clientescuentas.infrastructure.rest.dto.ClienteResponse;
import com.sergioapi.clientescuentas.infrastructure.rest.dto.CuentaBancariaResponse;
import org.springframework.stereotype.Component;

@Component
public class RestMapper {

    public ClienteResponse toClienteResponse(Cliente cliente) {
        return new ClienteResponse(
                cliente.dni(),
                cliente.nombre(),
                cliente.apellido1(),
                cliente.apellido2(),
                cliente.fechaNacimiento()
        );
    }

    public CuentaBancariaResponse toCuentaBancariaResponse(CuentaBancaria cuenta) {
        return new CuentaBancariaResponse(
                cuenta.id(),
                cuenta.dniCliente(),
                cuenta.tipoCuenta(),
                cuenta.total()
        );
    }

    public ClienteConCuentasResponse toClienteConCuentasResponse(
            ClienteConCuentas clienteConCuentas
    ) {
        Cliente cliente = clienteConCuentas.cliente();

        return new ClienteConCuentasResponse(
                cliente.dni(),
                cliente.nombre(),
                cliente.apellido1(),
                cliente.apellido2(),
                cliente.fechaNacimiento(),
                clienteConCuentas.cuentas().stream()
                        .map(this::toCuentaBancariaResponse)
                        .toList()
        );
    }
}
