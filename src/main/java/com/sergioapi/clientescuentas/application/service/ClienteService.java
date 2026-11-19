package com.sergioapi.clientescuentas.application.service;

import com.sergioapi.clientescuentas.application.exception.ClienteNotFoundException;
import com.sergioapi.clientescuentas.application.model.ClienteConCuentas;
import com.sergioapi.clientescuentas.application.port.in.ClienteUseCase;
import com.sergioapi.clientescuentas.application.port.out.ClienteRepository;
import com.sergioapi.clientescuentas.application.port.out.CuentaBancariaRepository;
import com.sergioapi.clientescuentas.domain.model.Cliente;
import com.sergioapi.clientescuentas.domain.model.CuentaBancaria;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class ClienteService implements ClienteUseCase {

    private final ClienteRepository clienteRepository;
    private final CuentaBancariaRepository cuentaRepository;
    private final Clock clock;

    @Override
    public List<ClienteConCuentas> getClientes(int page, int size) {
        List<Cliente> clientes = clienteRepository.findAll(page, size);

        if (clientes.isEmpty()) {
            return List.of();
        }

        List<String> dnis = clientes.stream()
                .map(Cliente::dni)
                .toList();

        Map<String, List<CuentaBancaria>> cuentasPorDni =
                cuentaRepository.findByDniClienteIn(dnis)
                        .stream()
                        .collect(Collectors.groupingBy(CuentaBancaria::dniCliente));

        return clientes.stream()
                .map(cliente -> new ClienteConCuentas(
                        cliente,
                        cuentasPorDni.getOrDefault(cliente.dni(), List.of())
                ))
                .toList();
    }

    @Override
    public List<Cliente> getClientesMayoresDeEdad(int page, int size) {
        LocalDate fechaLimite = LocalDate.now(clock).minusYears(18);

        return clienteRepository.findByFechaNacimientoHasta(
                fechaLimite, page, size
        );
    }

    @Override
    public List<Cliente> getClientesConCuentaSuperiorA(
            BigDecimal cantidad, int page, int size
    ) {
        return clienteRepository.findBySaldoTotalSuperiorA(
                cantidad, page, size
        );
    }

    @Override
    public ClienteConCuentas getClienteByDni(String dni) {
        return clienteRepository.findByDniWithCuentas(dni)
                .orElseThrow(() -> new ClienteNotFoundException(dni));
    }
}
