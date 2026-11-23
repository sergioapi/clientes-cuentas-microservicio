package com.sergioapi.clientescuentas.infrastructure.rest.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:cliente-controller-tests;DB_CLOSE_DELAY=-1",
        "spring.h2.console.enabled=false"
})
@AutoConfigureMockMvc
@Import(ClienteControllerIntegrationTest.FixedClockConfig.class)
class ClienteControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldReturnClientesWithCuentasBancarias() throws Exception {
        mockMvc.perform(get("/clientes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(5)))
                .andExpect(jsonPath("$[0].dni").value("11111111A"))
                .andExpect(jsonPath("$[0].cuentas", hasSize(2)))
                .andExpect(jsonPath("$[0].cuentas[0].id").value(1))
                .andExpect(jsonPath("$[0].cuentas[1].id").value(2))
                .andExpect(jsonPath("$[4].dni").value("55555555E"));
    }

    @Test
    void shouldPaginateClientesPreservingDniOrder() throws Exception {
        mockMvc.perform(get("/clientes")
                        .param("page", "1")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].dni").value("33333333C"))
                .andExpect(jsonPath("$[1].dni").value("44444444D"));
    }

    @Test
    void shouldReturnOnlyAdultClientes() throws Exception {
        mockMvc.perform(get("/clientes/mayores-de-edad"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(4)))
                .andExpect(jsonPath("$[0].dni").value("11111111A"))
                .andExpect(jsonPath("$[1].dni").value("22222222B"))
                .andExpect(jsonPath("$[2].dni").value("44444444D"))
                .andExpect(jsonPath("$[3].dni").value("55555555E"));
    }

    @Test
    void shouldReturnClientesWithSaldoStrictlyAboveThreshold() throws Exception {
        mockMvc.perform(get("/clientes/con-cuenta-superior-a/120000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].dni").value("11111111A"));
    }

    @Test
    void shouldReturnClienteByDniWithCuentasBancarias() throws Exception {
        mockMvc.perform(get("/clientes/11111111A"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dni").value("11111111A"))
                .andExpect(jsonPath("$.nombre").value("Juan"))
                .andExpect(jsonPath("$.cuentas", hasSize(2)))
                .andExpect(jsonPath("$.cuentas[0].tipoCuenta").value("PREMIUM"))
                .andExpect(jsonPath("$.cuentas[1].tipoCuenta").value("NORMAL"));
    }

    @Test
    void shouldReturnNotFoundForUnknownCliente() throws Exception {
        mockMvc.perform(get("/clientes/99999999A"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.detail",
                        containsString("99999999A")));
    }

    @ParameterizedTest
    @CsvSource({
            "page,-1",
            "size,0",
            "size,101"
    })
    void shouldRejectInvalidPaginationValues(
            String parameter, String value
    ) throws Exception {
        mockMvc.perform(get("/clientes").param(parameter, value))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void shouldRejectPaginationOffsetOverflow() throws Exception {
        mockMvc.perform(get("/clientes")
                        .param("page", "21474837")
                        .param("size", "100"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectInvalidDni() throws Exception {
        mockMvc.perform(get("/clientes/DNI-INVALIDO"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectInvalidCantidad() throws Exception {
        mockMvc.perform(get("/clientes/con-cuenta-superior-a/no-es-numero"))
                .andExpect(status().isBadRequest());
    }

    @TestConfiguration
    static class FixedClockConfig {

        @Bean
        @Primary
        Clock fixedTestClock() {
            return Clock.fixed(
                    Instant.parse("2026-11-23T12:00:00Z"),
                    ZoneId.of("Europe/Madrid")
            );
        }
    }
}
