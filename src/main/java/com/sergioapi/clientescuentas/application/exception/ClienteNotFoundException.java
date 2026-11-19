package com.sergioapi.clientescuentas.application.exception;

public class ClienteNotFoundException extends RuntimeException {

    public ClienteNotFoundException(String dni) {
        super("No existe un cliente con DNI " + dni);
    }
}
