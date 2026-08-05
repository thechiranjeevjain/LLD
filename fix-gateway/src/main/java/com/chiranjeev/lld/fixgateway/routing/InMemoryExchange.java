package com.chiranjeev.lld.fixgateway.routing;

import com.chiranjeev.lld.fixgateway.domain.ExecutionReport;
import com.chiranjeev.lld.fixgateway.domain.OrderRequest;

import java.util.concurrent.atomic.AtomicLong;

public final class InMemoryExchange implements OrderRouter {
    private final AtomicLong exchangeSequence = new AtomicLong(1);

    @Override
    public ExecutionReport route(OrderRequest order) {
        long id = exchangeSequence.getAndIncrement();
        return ExecutionReport.accepted(order, "EX-ORD-" + id, "EX-EXEC-" + id);
    }
}

