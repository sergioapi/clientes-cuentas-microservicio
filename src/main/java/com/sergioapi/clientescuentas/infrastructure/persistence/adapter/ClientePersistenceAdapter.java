package com.sergioapi.clientescuentas.infrastructure.persistence.adapter;

import com.sergioapi.clientescuentas.application.port.out.ClienteRepository;
import com.sergioapi.clientescuentas.domain.model.Cliente;
import com.sergioapi.clientescuentas.infrastructure.persistence.entity.ClienteJpaEntity;
import com.sergioapi.clientescuentas.infrastructure.persistence.repository.ClienteJpaRepository;
import jakarta.persistence.EntityManager;
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
    public void insert(Cliente cliente) {
        // El DNI se asigna en el dominio. Persist fuerza una inserción, no un merge.
        entityManager.persist(new ClienteJpaEntity(
                cliente.dni(), cliente.nombre(), cliente.apellido1(),
                cliente.apellido2(), cliente.fechaNacimiento()
        ));
        entityManager.flush();
    }

    private static PageRequest pageRequest(int page, int size) {
        return PageRequest.of(page, size, Sort.by("dni").ascending());
    }

    private static Cliente toDomain(ClienteJpaEntity entity) {
        return new Cliente(
                entity.getDni(), entity.getNombre(), entity.getApellido1(),
                entity.getApellido2(), entity.getFechaNacimiento()
        );
    }
}
