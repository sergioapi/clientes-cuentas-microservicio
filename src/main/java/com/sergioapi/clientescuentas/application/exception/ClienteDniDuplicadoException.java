package com.sergioapi.clientescuentas.application.exception;

public class ClienteDniDuplicadoException extends RuntimeException {

    public ClienteDniDuplicadoException(String dni, Throwable cause) {
        super("Ya existe un cliente con DNI " + dni, cause);
    }
}