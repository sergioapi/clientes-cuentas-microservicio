package com.sergioapi.clientescuentas.application.service;

import com.sergioapi.clientescuentas.application.exception.CuentaNotFoundException;
import com.sergioapi.clientescuentas.application.port.in.CuentaBancariaUseCase;
import com.sergioapi.clientescuentas.application.port.out.ClienteRepository;
import com.sergioapi.clientescuentas.application.port.out.CuentaBancariaRepository;
import com.sergioapi.clientescuentas.application.port.out.TransactionPort;
import com.sergioapi.clientescuentas.domain.model.Cliente;
import com.sergioapi.clientescuentas.domain.model.CuentaBancaria;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;

@RequiredArgsConstructor
public class CuentaBancariaService implements CuentaBancariaUseCase {

    private final ClienteRepository clienteRepository;
    private final CuentaBancariaRepository cuentaRepository;
    private final TransactionPort transactionPort;

    @Override
    public CuentaBancaria createCuenta(String dniCliente, String tipoCuenta, BigDecimal total) {
        CuentaBancaria cuenta = CuentaBancaria.crear(
                dniCliente, tipoCuenta, total
        );

        return transactionPort.execute(() -> {
            if (clienteRepository.findByDni(dniCliente).isEmpty()) {
                clienteRepository.insert(Cliente.crearConDni(dniCliente));
            }

            return cuentaRepository.save(cuenta);
        });
    }

    @Override
    public CuentaBancaria updateCuentaSaldo(Long idCuenta, BigDecimal total) {
        return transactionPort.execute(() -> {
            CuentaBancaria cuenta = cuentaRepository.findById(idCuenta)
                    .orElseThrow(() -> new CuentaNotFoundException(idCuenta));

            return cuentaRepository.save(cuenta.actualizarSaldo(total));
        });
    }
}
