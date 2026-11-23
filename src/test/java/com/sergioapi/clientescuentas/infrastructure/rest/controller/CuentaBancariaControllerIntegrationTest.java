package com.sergioapi.clientescuentas.infrastructure.rest.controller;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:cuenta-controller-tests;DB_CLOSE_DELAY=-1",
        "spring.h2.console.enabled=false"
})
@AutoConfigureMockMvc
class CuentaBancariaControllerIntegrationTest {
    private static final String NUEVO_DNI = "66666666F";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @AfterEach
    void cleanUp() {
        jdbcTemplate.update("DELETE FROM cuentas_bancarias WHERE id > 7");
        jdbcTemplate.update("DELETE FROM clientes WHERE dni = ?", NUEVO_DNI);
        jdbcTemplate.update(
                "UPDATE cuentas_bancarias SET total = 150000.00 WHERE id = 1"
        );
    }

    @Test
    void shouldCreateCuentaBancariaForExistingCliente() throws Exception {
        mockMvc.perform(post("/cuentas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "dniCliente": "11111111A",
                                  "tipoCuenta": " normal ",
                                  "total": 250.50
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.dniCliente").value("11111111A"))
                .andExpect(jsonPath("$.tipoCuenta").value("NORMAL"))
                .andExpect(jsonPath("$.total").value(250.50));

        mockMvc.perform(get("/clientes/11111111A"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cuentas", hasSize(3)));
    }

    @Test
    void shouldCreateClienteWhenCreatingCuentaBancaria() throws Exception {
        mockMvc.perform(post("/cuentas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "dniCliente": "66666666F",
                                  "tipoCuenta": "JUNIOR",
                                  "total": 100.25
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.dniCliente").value(NUEVO_DNI))
                .andExpect(jsonPath("$.tipoCuenta").value("JUNIOR"))
                .andExpect(jsonPath("$.total").value(100.25));

        mockMvc.perform(get("/clientes/{dni}", NUEVO_DNI))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dni").value(NUEVO_DNI))
                .andExpect(jsonPath("$.nombre").value(nullValue()))
                .andExpect(jsonPath("$.fechaNacimiento").value(nullValue()))
                .andExpect(jsonPath("$.cuentas", hasSize(1)))
                .andExpect(jsonPath("$.cuentas[0].tipoCuenta").value("JUNIOR"));
    }

    @Test
    void shouldUpdateCuentaBancariaSaldo() throws Exception {
        mockMvc.perform(put("/cuentas/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"total": 200.35}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.dniCliente").value("11111111A"))
                .andExpect(jsonPath("$.tipoCuenta").value("PREMIUM"))
                .andExpect(jsonPath("$.total").value(200.35));

        BigDecimal saldoPersistido = jdbcTemplate.queryForObject(
                "SELECT total FROM cuentas_bancarias WHERE id = 1",
                BigDecimal.class
        );

        assertThat(saldoPersistido).isEqualByComparingTo("200.35");
    }

    @Test
    void shouldReturnNotFoundForUnknownCuentaBancaria() throws Exception {
        mockMvc.perform(put("/cuentas/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"total": 200.35}
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.detail", containsString("999")));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            """
                    {"dniCliente":"INVALIDO","tipoCuenta":"NORMAL","total":100.25}
                    """,
            """
                    {"dniCliente":"66666666F","tipoCuenta":"   ","total":100.25}
                    """,
            """
                    {"dniCliente":"66666666F","tipoCuenta":"NORMAL"}
                    """,
            """
                    {"dniCliente":"66666666F","tipoCuenta":"NORMAL","total":100.251}
                    """
    })
    void shouldRejectInvalidCreateCuentaRequest(String body) throws Exception {
        mockMvc.perform(post("/cuentas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        assertThat(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM clientes WHERE dni = ?",
                Long.class, NUEVO_DNI
        )).isZero();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            """
                    {"total":null}
                    """,
            """
                    {"total":100.001}
                    """
    })
    void shouldRejectInvalidUpdateSaldoRequest(String body) throws Exception {
        mockMvc.perform(put("/cuentas/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        BigDecimal saldoPersistido = jdbcTemplate.queryForObject(
                "SELECT total FROM cuentas_bancarias WHERE id = 1",
                BigDecimal.class
        );

        assertThat(saldoPersistido).isEqualByComparingTo("150000.00");
    }

    @Test
    void shouldRejectInvalidCuentaBancariaId() throws Exception {
        mockMvc.perform(put("/cuentas/0")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"total":200.35}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectMalformedJson() throws Exception {
        mockMvc.perform(post("/cuentas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{invalid"))
                .andExpect(status().isBadRequest());
    }
}
