package com.sergioapi.clientescuentas.infrastructure.rest.controller;

import com.sergioapi.clientescuentas.application.port.in.ClienteUseCase;
import com.sergioapi.clientescuentas.infrastructure.rest.mapper.RestMapper;
import com.sergioapi.clientescuentas.infrastructure.rest.dto.ClienteConCuentasResponse;
import com.sergioapi.clientescuentas.infrastructure.rest.dto.ClienteResponse;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/clientes")
@RequiredArgsConstructor
public class ClienteController {

    private final ClienteUseCase clienteUseCase;
    private final RestMapper restMapper;

    @GetMapping
    public List<ClienteConCuentasResponse> getClientes(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size
    ) {
        return clienteUseCase.getClientes(page, size).stream()
                .map(restMapper::toClienteConCuentasResponse)
                .toList();
    }

    @GetMapping("/mayores-de-edad")
    public List<ClienteResponse> getClientesMayoresDeEdad(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size
    ) {
        return clienteUseCase.getClientesMayoresDeEdad(page, size).stream()
                .map(restMapper::toClienteResponse)
                .toList();
    }

    @GetMapping("/con-cuenta-superior-a/{cantidad}")
    public List<ClienteResponse> getClientesConCuentaSuperiorA(
            @PathVariable BigDecimal cantidad,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size
    ) {
        return clienteUseCase.getClientesConCuentaSuperiorA(cantidad, page, size)
                .stream()
                .map(restMapper::toClienteResponse)
                .toList();
    }

    @GetMapping("/{dni}")
    public ClienteConCuentasResponse getClienteByDni(
            @PathVariable
            @Pattern(regexp = "^[0-9]{8}[A-Z]$")
            String dni
    ) {
        return restMapper.toClienteConCuentasResponse(
                clienteUseCase.getClienteByDni(dni)
        );
    }
}
