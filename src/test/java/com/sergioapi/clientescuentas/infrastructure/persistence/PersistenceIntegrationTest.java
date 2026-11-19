package com.sergioapi.clientescuentas.infrastructure.persistence;

import com.sergioapi.clientescuentas.application.port.out.ClienteRepository;
import com.sergioapi.clientescuentas.application.port.out.CuentaBancariaRepository;
import com.sergioapi.clientescuentas.domain.model.Cliente;
import com.sergioapi.clientescuentas.domain.model.CuentaBancaria;
import com.sergioapi.clientescuentas.infrastructure.persistence.adapter.ClientePersistenceAdapter;
import com.sergioapi.clientescuentas.infrastructure.persistence.adapter.CuentaBancariaPersistenceAdapter;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({ClientePersistenceAdapter.class, CuentaBancariaPersistenceAdapter.class})
class PersistenceIntegrationTest {

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private CuentaBancariaRepository cuentaRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void shouldRetrievePaginatedClientesAndTheirAccounts() {
        var firstPage = clienteRepository.findAll(0, 2);
        var secondPage = clienteRepository.findAll(1, 2);
        var lastPage = clienteRepository.findAll(2, 2);

        assertThat(firstPage).extracting(Cliente::dni)
                .containsExactly("11111111A", "22222222B");
        assertThat(secondPage).extracting(Cliente::dni)
                .containsExactly("33333333C", "44444444D");
        assertThat(lastPage).extracting(Cliente::dni)
                .containsExactly("55555555E");
        assertThat(clienteRepository.findAll(3, 2)).isEmpty();

        var dnis = firstPage.stream().map(Cliente::dni).toList();
        assertThat(cuentaRepository.findByDniClienteIn(dnis))
                .extracting(CuentaBancaria::dniCliente)
                .containsExactlyInAnyOrder("11111111A", "11111111A", "22222222B", "22222222B");
    }

    @Test
    void shouldFilterByBirthDateIncludingBoundaryAndExcludingUnknownDate() {
        var cutoff = LocalDate.of(2008, 11, 19);
        clienteRepository.insert(new Cliente("66666666F", null, null, null, cutoff));
        clienteRepository.insert(new Cliente("77777777G", null, null, null, cutoff.plusDays(1)));
        clienteRepository.insert(Cliente.crearConDni("88888888H"));
        entityManager.flush();
        entityManager.clear();

        assertThat(clienteRepository.findByFechaNacimientoHasta(cutoff, 0, 20))
                .extracting(Cliente::dni)
                .containsExactly("11111111A", "22222222B", "44444444D", "55555555E", "66666666F");
        assertThat(clienteRepository.findByFechaNacimientoHasta(cutoff, 1, 2))
                .extracting(Cliente::dni)
                .containsExactly("44444444D", "55555555E");
    }

    @Test
    void shouldFilterByAccumulatedBalanceAndExcludeEqualThreshold() {
        assertThat(clienteRepository.findBySaldoTotalSuperiorA(new BigDecimal("50000"), 0, 20))
                .extracting(Cliente::dni)
                .containsExactly("11111111A", "22222222B", "44444444D", "55555555E");

        assertThat(clienteRepository.findBySaldoTotalSuperiorA(new BigDecimal("50300"), 0, 20))
                .extracting(Cliente::dni)
                .containsExactly("11111111A", "44444444D", "55555555E");

        assertThat(clienteRepository.findBySaldoTotalSuperiorA(new BigDecimal("50000"), 1, 2))
                .extracting(Cliente::dni)
                .containsExactly("44444444D", "55555555E");
    }

    @Test
    void shouldFindClienteWithItsAccountsByDni() {
        assertThat(clienteRepository.findByDni("11111111A"))
                .hasValueSatisfying(cliente -> assertThat(cliente.nombre()).isEqualTo("Juan"));
        assertThat(clienteRepository.findByDni("99999999A")).isEmpty();
        assertThat(cuentaRepository.findByDniClienteIn(List.of("11111111A")))
                .hasSize(2)
                .allSatisfy(cuenta -> assertThat(cuenta.dniCliente()).isEqualTo("11111111A"));
    }

    @Test
    void shouldPersistAccountForNewClienteAndUpdateItsBalance() {
        var cliente = Cliente.crearConDni("66666666F");
        clienteRepository.insert(cliente);

        var cuenta = cuentaRepository.save(CuentaBancaria.crear(
                cliente.dni(), "NORMAL", new BigDecimal("100.25")
        ));
        entityManager.flush();
        entityManager.clear();

        assertThat(clienteRepository.findByDni(cliente.dni())).contains(cliente);
        assertThat(cuenta.id()).isNotNull();
        assertThat(cuentaRepository.findById(cuenta.id())).contains(cuenta);

        var actualizada = cuenta.actualizarSaldo(new BigDecimal("200.35"));
        cuentaRepository.save(actualizada);
        entityManager.flush();
        entityManager.clear();

        assertThat(cuentaRepository.findById(cuenta.id())).contains(actualizada);
        assertThat(cuentaRepository.findByDniClienteIn(List.of(cliente.dni())))
                .containsExactly(actualizada);
    }
}
