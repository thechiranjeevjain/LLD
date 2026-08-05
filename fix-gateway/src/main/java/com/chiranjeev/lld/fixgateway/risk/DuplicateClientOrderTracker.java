package com.chiranjeev.lld.fixgateway.risk;

import com.chiranjeev.lld.fixgateway.domain.OrderRequest;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class DuplicateClientOrderTracker {
    private final Set<String> seenOrderIds = ConcurrentHashMap.newKeySet();

    public boolean reserve(OrderRequest order) {
        return seenOrderIds.add(order.clientCompId() + ":" + order.clientOrderId());
    }
}

