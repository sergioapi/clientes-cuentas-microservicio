package com.sergioapi.clientescuentas.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "cuentas_bancarias")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CuentaBancariaJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "dni_cliente", length = 9, nullable = false)
    private String dniCliente;

    @Column(name = "tipo_cuenta", length = 50, nullable = false)
    private String tipoCuenta;

    @Column(name = "total", precision = 19, scale = 2, nullable = false)
    private BigDecimal total;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "dni_cliente",
            referencedColumnName = "dni",
            insertable = false,
            updatable = false
    )
    private ClienteJpaEntity cliente;

    public CuentaBancariaJpaEntity(
            Long id,
            String dniCliente,
            String tipoCuenta,
            BigDecimal total
    ) {
        this.id = id;
        this.dniCliente = dniCliente;
        this.tipoCuenta = tipoCuenta;
        this.total = total;
    }
}
