package com.sergioapi.clientescuentas.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "cuentas_bancarias")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
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
}
