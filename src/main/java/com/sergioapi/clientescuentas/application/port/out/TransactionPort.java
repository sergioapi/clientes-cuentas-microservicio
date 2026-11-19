package com.sergioapi.clientescuentas.application.port.out;

import java.util.function.Supplier;

public interface TransactionPort {

    <T> T execute(Supplier<T> operation);
}
