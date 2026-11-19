package com.sergioapi.clientescuentas.application.port.out;

import com.sergioapi.clientescuentas.domain.model.CuentaBancaria;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface CuentaBancariaRepository {

    List<CuentaBancaria> findByDniClienteIn(Collection<String> dnis);

    Optional<CuentaBancaria> findById(Long id);

    CuentaBancaria save(CuentaBancaria cuenta);
}
