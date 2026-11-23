package com.sergioapi.clientescuentas.infrastructure.rest.dto;

import java.time.LocalDate;
import java.util.List;

public record ClienteConCuentasResponse(
        String dni,
        String nombre,
        String apellido1,
        String apellido2,
        LocalDate fechaNacimiento,
        List<CuentaBancariaResponse> cuentas
) {
}
