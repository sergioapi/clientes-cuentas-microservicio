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
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cuentas")
@RequiredArgsConstructor
public class CuentaBancariaController {

    private final CuentaBancariaUseCase cuentaBancariaUseCase;
    private final RestMapper restMapper;

    @PostMapping
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

    @PutMapping("/{idCuenta}")
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
