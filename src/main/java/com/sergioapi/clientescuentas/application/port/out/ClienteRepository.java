package com.sergioapi.clientescuentas.application.port.out;

import com.sergioapi.clientescuentas.application.model.ClienteConCuentas;
import com.sergioapi.clientescuentas.domain.model.Cliente;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ClienteRepository {

    List<Cliente> findAll(int page, int size);

    List<Cliente> findByFechaNacimientoHasta(LocalDate fechaLimite, int page, int size);

    List<Cliente> findBySaldoTotalSuperiorA(BigDecimal cantidad, int page, int size);

    Optional<Cliente> findByDni(String dni);

    Optional<ClienteConCuentas> findByDniWithCuentas(String dni);

    void insert(Cliente cliente);
}
