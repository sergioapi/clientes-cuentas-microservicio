package com.sergioapi.clientescuentas.domain.model;

import java.time.LocalDate;
import java.util.regex.Pattern;

public record Cliente(
        String dni,
        String nombre,
        String apellido1,
        String apellido2,
        LocalDate fechaNacimiento
) {
    private static final Pattern DNI_PATTERN = Pattern.compile("[0-9]{8}[A-Z]");

    public Cliente {
        validateDni(dni);
    }

    public static Cliente crearConDni(String dni) {
        return new Cliente(dni, null, null, null, null);
    }

    static void validateDni(String dni) {
        if (dni == null || !DNI_PATTERN.matcher(dni).matches()) {
            throw new IllegalArgumentException(
                    "El DNI debe contener ocho dígitos y una letra mayúscula"
            );
        }
    }
}
