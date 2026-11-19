package com.sergioapi.clientescuentas.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "clientes")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
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

    @OneToMany(mappedBy = "cliente", fetch = FetchType.LAZY)
    @OrderBy("id ASC")
    private List<CuentaBancariaJpaEntity> cuentas = new ArrayList<>();

    public ClienteJpaEntity(
            String dni,
            String nombre,
            String apellido1,
            String apellido2,
            LocalDate fechaNacimiento
    ) {
        this.dni = dni;
        this.nombre = nombre;
        this.apellido1 = apellido1;
        this.apellido2 = apellido2;
        this.fechaNacimiento = fechaNacimiento;
    }
}
