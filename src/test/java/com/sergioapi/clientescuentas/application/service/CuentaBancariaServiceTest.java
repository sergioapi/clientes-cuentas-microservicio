package com.sergioapi.clientescuentas.application.service;

import com.sergioapi.clientescuentas.application.exception.ClienteDniDuplicadoException;
import com.sergioapi.clientescuentas.application.exception.CuentaNotFoundException;
import com.sergioapi.clientescuentas.application.port.out.ClienteRepository;
import com.sergioapi.clientescuentas.application.port.out.CuentaBancariaRepository;
import com.sergioapi.clientescuentas.application.port.out.TransactionPort;
import com.sergioapi.clientescuentas.domain.model.Cliente;
import com.sergioapi.clientescuentas.domain.model.CuentaBancaria;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CuentaBancariaServiceTest {

    private static final String DNI = "66666666F";
    private static final BigDecimal TOTAL = new BigDecimal("100.25");

    @Mock
    private ClienteRepository clienteRepository;
    @Mock
    private CuentaBancariaRepository cuentaRepository;
    @Mock
    private TransactionPort transactionPort;
    @InjectMocks
    private CuentaBancariaService service;

    @BeforeEach
    void setUp() {
        when(transactionPort.execute(any())).thenAnswer(invocation -> {
            Supplier<?> operation = invocation.getArgument(0);
            return operation.get();
        });
    }

    @Test
    void shouldCreateCuentaBancariaForExistingCliente() {
        var nueva = CuentaBancaria.crear(DNI, "NORMAL", TOTAL);
        var guardada = new CuentaBancaria(8L, DNI, "NORMAL", TOTAL);
        when(clienteRepository.findByDni(DNI)).thenReturn(Optional.of(Cliente.crearConDni(DNI)));
        when(cuentaRepository.save(nueva)).thenReturn(guardada);

        assertThat(service.createCuenta(DNI, "NORMAL", TOTAL)).isEqualTo(guardada);
        verify(clienteRepository, never()).insert(any());
    }

    @Test
    void shouldCreateClienteBeforeSavingCuentaBancaria() {
        var cliente = Cliente.crearConDni(DNI);
        var nueva = CuentaBancaria.crear(DNI, "NORMAL", TOTAL);
        var guardada = new CuentaBancaria(8L, DNI, "NORMAL", TOTAL);
        when(clienteRepository.findByDni(DNI)).thenReturn(Optional.empty());
        when(cuentaRepository.save(nueva)).thenReturn(guardada);

        assertThat(service.createCuenta(DNI, "NORMAL", TOTAL)).isEqualTo(guardada);
        var orden = inOrder(clienteRepository, cuentaRepository);

        orden.verify(clienteRepository).findByDni(DNI);
        orden.verify(clienteRepository).insert(cliente);
        orden.verify(cuentaRepository).save(nueva);
    }

    @Test
    void shouldUpdateCuentaBancariaSaldo() {
        var cuenta = new CuentaBancaria(8L, DNI, "NORMAL", TOTAL);
        var nuevoTotal = new BigDecimal("200.35");
        var actualizada = new CuentaBancaria(8L, DNI, "NORMAL", nuevoTotal);

        when(cuentaRepository.findById(8L))
                .thenReturn(Optional.of(cuenta));
        when(cuentaRepository.save(actualizada))
                .thenReturn(actualizada);

        assertThat(service.updateCuentaSaldo(8L, nuevoTotal))
                .isEqualTo(actualizada);
    }

    @Test
    void shouldRejectUpdateForUnknownCuentaBancaria() {
        when(cuentaRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updateCuentaSaldo(99L, TOTAL))
                .isInstanceOf(CuentaNotFoundException.class)
                .hasMessageContaining("99");

        verify(cuentaRepository, never()).save(any());
    }

    @Test
    void shouldRetryCreateCuentaOnceWhenClienteAlreadyCreatedConcurrently() {
        var nueva = CuentaBancaria.crear(DNI, "NORMAL", TOTAL);
        var guardada = new CuentaBancaria(8L, DNI, "NORMAL", TOTAL);
        var cliente = Cliente.crearConDni(DNI);

        when(clienteRepository.findByDni(DNI))
                .thenReturn(Optional.empty(), Optional.of(cliente));

        doThrow(new ClienteDniDuplicadoException(
                DNI, new RuntimeException("DNI duplicado")
        )).when(clienteRepository).insert(cliente);

        when(cuentaRepository.save(nueva)).thenReturn(guardada);

        assertThat(service.createCuenta(DNI, "NORMAL", TOTAL))
                .isEqualTo(guardada);

        verify(transactionPort, times(2)).execute(any());
        verify(clienteRepository, times(2)).findByDni(DNI);
        verify(clienteRepository, times(1)).insert(cliente);
        verify(cuentaRepository, times(1)).save(nueva);
    }

    @Test
    void shouldNotRetryCreateCuentaOnUnexpectedFailure() {
        var cliente = Cliente.crearConDni(DNI);

        when(clienteRepository.findByDni(DNI))
                .thenReturn(Optional.empty());

        doThrow(new IllegalStateException("Fallo inesperado"))
                .when(clienteRepository).insert(cliente);

        assertThatThrownBy(() -> service.createCuenta(DNI, "NORMAL", TOTAL))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Fallo inesperado");

        verify(transactionPort, times(1)).execute(any());
        verify(cuentaRepository, never()).save(any());
    }

    @Test
    void shouldStopRetryingAfterSecondDniConflict() {
        var cliente = Cliente.crearConDni(DNI);

        when(clienteRepository.findByDni(DNI))
                .thenReturn(Optional.empty());

        doThrow(new ClienteDniDuplicadoException(
                DNI, new RuntimeException("DNI duplicado")
        )).when(clienteRepository).insert(cliente);

        assertThatThrownBy(() -> service.createCuenta(DNI, "NORMAL", TOTAL))
                .isInstanceOf(ClienteDniDuplicadoException.class);

        verify(transactionPort, times(2)).execute(any());
        verify(clienteRepository, times(2)).insert(cliente);
        verify(cuentaRepository, never()).save(any());
    }
}
