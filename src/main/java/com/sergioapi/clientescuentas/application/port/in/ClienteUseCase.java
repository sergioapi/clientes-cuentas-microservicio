package com.sergioapi.clientescuentas.application.port.in;

import com.sergioapi.clientescuentas.application.model.ClienteConCuentas;
import com.sergioapi.clientescuentas.domain.model.Cliente;

import java.math.BigDecimal;
import java.util.List;

public interface ClienteUseCase {
    List<ClienteConCuentas> getClientes(int page, int size);

    List<Cliente> getClientesMayoresDeEdad(int page, int size);

    List<Cliente> getClientesConCuentaSuperiorA(
            BigDecimal cantidad, int page, int size
    );

    ClienteConCuentas getClienteByDni(String dni);
}
