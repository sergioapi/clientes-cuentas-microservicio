package com.sergioapi.clientescuentas.infrastructure.persistence.adapter;

import com.sergioapi.clientescuentas.application.port.out.TransactionPort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.function.Supplier;

@Component
public class TransactionAdapter implements TransactionPort {

    private final TransactionTemplate writeTemplate;

    public TransactionAdapter(PlatformTransactionManager transactionManager) {
        this.writeTemplate = new TransactionTemplate(transactionManager);
    }

    @Override
    public <T> T execute(Supplier<T> operation) {
        return writeTemplate.execute(status -> operation.get());
    }
}
