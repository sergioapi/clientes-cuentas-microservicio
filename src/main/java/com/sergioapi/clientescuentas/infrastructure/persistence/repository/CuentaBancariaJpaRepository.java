package com.sergioapi.clientescuentas.infrastructure.persistence.repository;

import com.sergioapi.clientescuentas.infrastructure.persistence.entity.CuentaBancariaJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface CuentaBancariaJpaRepository extends JpaRepository<CuentaBancariaJpaEntity, Long> {

    List<CuentaBancariaJpaEntity> findByDniClienteInOrderByIdAsc(Collection<String> dnis);
}
