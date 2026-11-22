package com.sergioapi.clientescuentas.application.service;

import com.sergioapi.clientescuentas.application.exception.ClienteNotFoundException;
import com.sergioapi.clientescuentas.application.model.ClienteConCuentas;
import com.sergioapi.clientescuentas.application.port.out.ClienteRepository;
import com.sergioapi.clientescuentas.application.port.out.CuentaBancariaRepository;
import com.sergioapi.clientescuentas.domain.model.Cliente;
import com.sergioapi.clientescuentas.domain.model.CuentaBancaria;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClienteServiceTest {

    @Mock
    private ClienteRepository clienteRepository;
    @Mock
    private CuentaBancariaRepository cuentaRepository;

    private ClienteService service;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(Instant.parse("2026-11-19T12:00:00Z"),
                ZoneId.of("Europe/Madrid"));
        service = new ClienteService(clienteRepository, cuentaRepository, clock);
    }

    @Test
    void shouldGroupCuentasByClientePreservingPageOrder() {
        var primero = Cliente.crearConDni("11111111A");
        var segundo = Cliente.crearConDni("22222222B");
        var sinCuentas = Cliente.crearConDni("33333333C");
        var cuenta1 = new CuentaBancaria(1L, primero.dni(), "NORMAL", BigDecimal.TEN);
        var cuenta2 = new CuentaBancaria(2L, segundo.dni(), "PREMIUM", BigDecimal.TEN);
        var cuenta3 = new CuentaBancaria(3L, primero.dni(), "JUNIOR", BigDecimal.ONE);
        var dnis = List.of(primero.dni(), segundo.dni(), sinCuentas.dni());

        when(clienteRepository.findAll(1, 3)).thenReturn(List.of(primero, segundo, sinCuentas));
        when(cuentaRepository.findByDniClienteIn(dnis)).thenReturn(List.of(cuenta1, cuenta2, cuenta3));

        assertThat(service.getClientes(1, 3)).containsExactly(
                new ClienteConCuentas(primero, List.of(cuenta1, cuenta3)),
                new ClienteConCuentas(segundo, List.of(cuenta2)),
                new ClienteConCuentas(sinCuentas, List.of()));
        verify(cuentaRepository).findByDniClienteIn(dnis);
    }

    @Test
    void shouldSkipCuentasQueryWhenPageIsEmpty() {
        when(clienteRepository.findAll(5, 20)).thenReturn(List.of());

        assertThat(service.getClientes(5, 20)).isEmpty();
        verifyNoInteractions(cuentaRepository);
    }

    @Test
    void shouldCalculateAdultCutoffUsingFixedClock() {
        var cliente = Cliente.crearConDni("11111111A");
        var cutoff = LocalDate.of(2008, 11, 19);
        when(clienteRepository.findByFechaNacimientoHasta(cutoff, 0, 20))
                .thenReturn(List.of(cliente));

        assertThat(service.getClientesMayoresDeEdad(0, 20)).containsExactly(cliente);
    }

    @Test
    void shouldReturnClientesWithTotalBalanceAboveThreshold() {
        var cantidad = new BigDecimal("50000.00");
        var cliente = Cliente.crearConDni("11111111A");

        when(clienteRepository.findBySaldoTotalSuperiorA(cantidad, 1, 5))
                .thenReturn(List.of(cliente));

        assertThat(service.getClientesConCuentaSuperiorA(cantidad, 1, 5))
                .containsExactly(cliente);
    }

    @Test
    void shouldReturnClienteWithCuentas() {
        var cliente = Cliente.crearConDni("11111111A");
        var cuenta = new CuentaBancaria(1L, cliente.dni(), "NORMAL", BigDecimal.TEN);
        var resultado = new ClienteConCuentas(cliente, List.of(cuenta));
        when(clienteRepository.findByDniWithCuentas(cliente.dni())).thenReturn(Optional.of(resultado));

        assertThat(service.getClienteByDni(cliente.dni())).isEqualTo(resultado);
    }

    @Test
    void shouldRejectUnknownCliente() {
        String dni = "99999999A";
        when(clienteRepository.findByDniWithCuentas(dni)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getClienteByDni(dni))
                .isInstanceOf(ClienteNotFoundException.class)
                .hasMessageContaining(dni);
    }
}
