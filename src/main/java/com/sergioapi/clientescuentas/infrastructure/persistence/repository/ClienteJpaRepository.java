package com.sergioapi.clientescuentas.infrastructure.persistence.repository;

import com.sergioapi.clientescuentas.infrastructure.persistence.entity.ClienteJpaEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface ClienteJpaRepository extends JpaRepository<ClienteJpaEntity, String> {

    List<ClienteJpaEntity> findAllBy(Pageable pageable);

    List<ClienteJpaEntity> findByFechaNacimientoLessThanEqual(
            LocalDate fechaLimite, Pageable pageable
    );

    @Query("""
            SELECT c FROM ClienteJpaEntity c
            WHERE c.dni IN (
                SELECT cb.dniCliente FROM CuentaBancariaJpaEntity cb
                GROUP BY cb.dniCliente
                HAVING SUM(cb.total) > :cantidad
            )
            """)
    List<ClienteJpaEntity> findBySaldoTotalSuperiorA(
            @Param("cantidad") BigDecimal cantidad, Pageable pageable
    );
}
