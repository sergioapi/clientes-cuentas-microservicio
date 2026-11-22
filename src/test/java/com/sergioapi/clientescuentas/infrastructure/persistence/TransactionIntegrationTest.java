package com.sergioapi.clientescuentas.infrastructure.persistence;

import com.sergioapi.clientescuentas.application.port.in.CuentaBancariaUseCase;
import com.sergioapi.clientescuentas.application.port.out.ClienteRepository;
import com.sergioapi.clientescuentas.application.port.out.CuentaBancariaRepository;
import com.sergioapi.clientescuentas.application.port.out.TransactionPort;
import com.sergioapi.clientescuentas.application.service.CuentaBancariaService;
import com.sergioapi.clientescuentas.domain.model.CuentaBancaria;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:transaction-tests;DB_CLOSE_DELAY=-1",
        "spring.h2.console.enabled=false"
})
class TransactionIntegrationTest {

    @Autowired
    private ClienteRepository clienteRepository;
    @Autowired
    private CuentaBancariaRepository cuentaRepository;
    @Autowired
    private TransactionPort transactionPort;
    @Autowired
    private CuentaBancariaUseCase cuentaUseCase;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void shouldCommitClienteAndCuentaBancaria() {
        String dni = "77777777G";
        assertThat(clienteRepository.findByDni(dni)).isEmpty();

        var cuenta = cuentaUseCase.createCuenta(dni, "NORMAL", new BigDecimal("100.25"));

        assertThat(clienteRepository.findByDni(dni)).isPresent();
        assertThat(cuenta.id()).isNotNull();
        assertThat(cuentaRepository.findById(cuenta.id())).contains(cuenta);
    }

    @Test
    void shouldRollbackClienteAndCuentaBancariaWhenSavingFails() {
        String dni = "88888888H";
        assertThat(clienteRepository.findByDni(dni)).isEmpty();

        var failingRepository = mock(CuentaBancariaRepository.class);
        when(failingRepository.save(any(CuentaBancaria.class))).thenAnswer(invocation -> {
            CuentaBancaria cuenta = invocation.getArgument(0);
            cuentaRepository.save(cuenta);
            assertThat(jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM clientes WHERE dni = ?", Long.class, dni)).isEqualTo(1L);
            assertThat(jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM cuentas_bancarias WHERE dni_cliente = ?", Long.class, dni))
                    .isEqualTo(1L);
            throw new IllegalStateException("Fallo simulado tras guardar la cuenta");
        });
        var service = new CuentaBancariaService(clienteRepository, failingRepository, transactionPort);

        assertThatThrownBy(() -> service.createCuenta(dni, "NORMAL", new BigDecimal("100.25")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Fallo simulado tras guardar la cuenta");

        assertThat(clienteRepository.findByDni(dni)).isEmpty();
        assertThat(cuentaRepository.findByDniClienteIn(List.of(dni))).isEmpty();
    }
}
