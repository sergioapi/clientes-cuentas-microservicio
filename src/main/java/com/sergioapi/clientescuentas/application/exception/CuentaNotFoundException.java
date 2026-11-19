package com.sergioapi.clientescuentas.application.exception;

public class CuentaNotFoundException extends RuntimeException {

    public CuentaNotFoundException(Long idCuenta) {
        super("No existe una cuenta bancaria con ID " + idCuenta);
    }
}
