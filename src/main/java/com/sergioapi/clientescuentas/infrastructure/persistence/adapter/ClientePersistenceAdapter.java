package com.sergioapi.clientescuentas.infrastructure.persistence.adapter;

import com.sergioapi.clientescuentas.application.exception.ClienteDniDuplicadoException;
import com.sergioapi.clientescuentas.application.model.ClienteConCuentas;
import com.sergioapi.clientescuentas.application.port.out.ClienteRepository;
import com.sergioapi.clientescuentas.domain.model.Cliente;
import com.sergioapi.clientescuentas.infrastructure.persistence.entity.ClienteJpaEntity;
import com.sergioapi.clientescuentas.infrastructure.persistence.repository.ClienteJpaRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ClientePersistenceAdapter implements ClienteRepository {

    private final ClienteJpaRepository repository;
    private final EntityManager entityManager;

    @Override
    public List<Cliente> findAll(int page, int size) {
        return repository.findAllBy(pageRequest(page, size))
                .stream()
                .map(ClientePersistenceAdapter::toDomain)
                .toList();
    }

    @Override
    public List<Cliente> findByFechaNacimientoHasta(LocalDate fechaLimite, int page, int size) {
        return repository.findByFechaNacimientoLessThanEqual(fechaLimite, pageRequest(page, size))
                .stream()
                .map(ClientePersistenceAdapter::toDomain)
                .toList();
    }

    @Override
    public List<Cliente> findBySaldoTotalSuperiorA(BigDecimal cantidad, int page, int size) {
        return repository.findBySaldoTotalSuperiorA(cantidad, pageRequest(page, size))
                .stream()
                .map(ClientePersistenceAdapter::toDomain)
                .toList();
    }

    @Override
    public Optional<Cliente> findByDni(String dni) {
        return repository.findById(dni).map(ClientePersistenceAdapter::toDomain);
    }

    @Override
    public Optional<ClienteConCuentas> findByDniWithCuentas(String dni) {
        return repository.findByDniWithCuentas(dni)
                .map(entity -> new ClienteConCuentas(
                        toDomain(entity),
                        entity.getCuentas().stream()
                                .map(CuentaBancariaPersistenceAdapter::toDomain)
                                .toList()
                ));
    }

    @Override
    public void insert(Cliente cliente) {
        try {
            entityManager.persist(new ClienteJpaEntity(
                    cliente.dni(), cliente.nombre(), cliente.apellido1(),
                    cliente.apellido2(), cliente.fechaNacimiento()
            ));
            entityManager.flush();

        } catch (PersistenceException ex) {
            if (esViolacionUnicidad(ex)) {
                throw new ClienteDniDuplicadoException(cliente.dni(), ex);
            }
            throw ex;
        }
    }

    private static boolean esViolacionUnicidad(Throwable exception) {
        Throwable causa = exception;

        while (causa != null) {
            if (causa instanceof org.hibernate.exception.ConstraintViolationException ex
                    && "23505".equals(ex.getSQLState())) {
                return true;
            }
            causa = causa.getCause();
        }

        return false;
    }

    private static PageRequest pageRequest(int page, int size) {
        if ((long) page * size > Integer.MAX_VALUE) {
            throw new IllegalArgumentException(
                    "El desplazamiento de paginación supera el límite permitido"
            );
        }

        return PageRequest.of(page, size, Sort.by("dni").ascending());
    }

    private static Cliente toDomain(ClienteJpaEntity entity) {
        return new Cliente(
                entity.getDni(), entity.getNombre(), entity.getApellido1(),
                entity.getApellido2(), entity.getFechaNacimiento()
        );
    }
}
