package com.sergioapi.clientescuentas.infrastructure.persistence.adapter;

import com.sergioapi.clientescuentas.application.port.out.CuentaBancariaRepository;
import com.sergioapi.clientescuentas.domain.model.CuentaBancaria;
import com.sergioapi.clientescuentas.infrastructure.persistence.entity.CuentaBancariaJpaEntity;
import com.sergioapi.clientescuentas.infrastructure.persistence.repository.CuentaBancariaJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class CuentaBancariaPersistenceAdapter implements CuentaBancariaRepository {

    private final CuentaBancariaJpaRepository repository;

    @Override
    public List<CuentaBancaria> findByDniClienteIn(Collection<String> dnis) {
        if (dnis.isEmpty()) {
            return List.of();
        }
        return repository.findByDniClienteInOrderByIdAsc(dnis)
                .stream()
                .map(CuentaBancariaPersistenceAdapter::toDomain)
                .toList();
    }

    @Override
    public Optional<CuentaBancaria> findById(Long id) {
        return repository.findById(id).map(CuentaBancariaPersistenceAdapter::toDomain);
    }

    @Override
    public CuentaBancaria save(CuentaBancaria cuenta) {
        var entity = new CuentaBancariaJpaEntity(
                cuenta.id(), cuenta.dniCliente(), cuenta.tipoCuenta(), cuenta.total()
        );
        return toDomain(repository.save(entity));
    }

    private static CuentaBancaria toDomain(CuentaBancariaJpaEntity entity) {
        return new CuentaBancaria(
                entity.getId(), entity.getDniCliente(), entity.getTipoCuenta(), entity.getTotal()
        );
    }
}
