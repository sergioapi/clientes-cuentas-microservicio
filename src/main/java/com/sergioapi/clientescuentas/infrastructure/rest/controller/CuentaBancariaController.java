package com.sergioapi.clientescuentas.infrastructure.rest.controller;

import com.sergioapi.clientescuentas.application.port.in.CuentaBancariaUseCase;
import com.sergioapi.clientescuentas.infrastructure.rest.mapper.RestMapper;
import com.sergioapi.clientescuentas.infrastructure.rest.dto.CreateCuentaRequest;
import com.sergioapi.clientescuentas.infrastructure.rest.dto.CuentaBancariaResponse;
import com.sergioapi.clientescuentas.infrastructure.rest.dto.UpdateSaldoRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(
        value = "/cuentas",
        produces = MediaType.APPLICATION_JSON_VALUE
)
@RequiredArgsConstructor
public class CuentaBancariaController {

    private final CuentaBancariaUseCase cuentaBancariaUseCase;
    private final RestMapper restMapper;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public CuentaBancariaResponse createCuenta(
            @Valid @RequestBody CreateCuentaRequest request
    ) {
        return restMapper.toCuentaBancariaResponse(
                cuentaBancariaUseCase.createCuenta(
                        request.dniCliente(),
                        request.tipoCuenta(),
                        request.total()
                )
        );
    }

    @PutMapping(
            value = "/{idCuenta}",
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    public CuentaBancariaResponse updateSaldo(
            @PathVariable @Positive Long idCuenta,
            @Valid @RequestBody UpdateSaldoRequest request
    ) {
        return restMapper.toCuentaBancariaResponse(
                cuentaBancariaUseCase.updateCuentaSaldo(
                        idCuenta,
                        request.total()
                )
        );
    }
}
