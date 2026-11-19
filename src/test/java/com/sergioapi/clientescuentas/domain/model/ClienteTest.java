package com.sergioapi.clientescuentas.domain.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ClienteTest {

    @Test
    void shouldCreateClienteWithOnlyDni() {
        var cliente = Cliente.crearConDni("11111111A");

        assertThat(cliente).isEqualTo(new Cliente("11111111A", null, null, null, null));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"1234567A", "12345678", "12345678a", "ABCDEFGHA"})
    void shouldRejectInvalidDni(String dni) {
        assertThatThrownBy(() -> Cliente.crearConDni(dni))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
