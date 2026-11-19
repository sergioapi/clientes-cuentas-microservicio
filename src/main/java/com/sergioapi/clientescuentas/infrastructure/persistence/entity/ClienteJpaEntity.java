package com.sergioapi.clientescuentas.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "clientes")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class ClienteJpaEntity {

    @Id
    @Column(name = "dni", length = 9, nullable = false)
    private String dni;

    @Column(name = "nombre", length = 100)
    private String nombre;

    @Column(name = "apellido1", length = 100)
    private String apellido1;

    @Column(name = "apellido2", length = 100)
    private String apellido2;

    @Column(name = "fecha_nacimiento")
    private LocalDate fechaNacimiento;
}
