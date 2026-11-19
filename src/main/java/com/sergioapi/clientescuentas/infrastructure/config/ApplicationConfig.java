package com.sergioapi.clientescuentas.infrastructure.config;

import com.sergioapi.clientescuentas.application.port.in.ClienteUseCase;
import com.sergioapi.clientescuentas.application.port.in.CuentaBancariaUseCase;
import com.sergioapi.clientescuentas.application.port.out.ClienteRepository;
import com.sergioapi.clientescuentas.application.port.out.CuentaBancariaRepository;
import com.sergioapi.clientescuentas.application.port.out.TransactionPort;
import com.sergioapi.clientescuentas.application.service.ClienteService;
import com.sergioapi.clientescuentas.application.service.CuentaBancariaService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;

@Configuration
public class ApplicationConfig {

    @Bean
    public Clock clock() {
        return Clock.system(ZoneId.of("Europe/Madrid"));
    }

    @Bean
    public ClienteUseCase clienteUseCase(
            ClienteRepository clienteRepository,
            CuentaBancariaRepository cuentaRepository,
            Clock clock
    ) {
        return new ClienteService(
                clienteRepository,
                cuentaRepository,
                clock
        );
    }

    @Bean
    public CuentaBancariaUseCase cuentaBancariaUseCase(
            ClienteRepository clienteRepository,
            CuentaBancariaRepository cuentaRepository,
            TransactionPort transactionPort
    ) {
        return new CuentaBancariaService(
                clienteRepository,
                cuentaRepository,
                transactionPort
        );
    }
}
