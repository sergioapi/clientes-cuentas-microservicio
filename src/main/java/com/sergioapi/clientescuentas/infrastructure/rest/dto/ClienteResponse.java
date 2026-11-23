package com.sergioapi.clientescuentas.infrastructure.rest.dto;

import java.time.LocalDate;

public record ClienteResponse(
        String dni,
        String nombre,
        String apellido1,
        String apellido2,
        LocalDate fechaNacimiento
) {
}
