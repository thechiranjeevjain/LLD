package com.chiranjeev.lld.fixgateway.risk;

import com.chiranjeev.lld.fixgateway.domain.OrderRequest;

public final class MaxQuantityRule implements RiskRule {
    private final int maxQuantity;

    public MaxQuantityRule(int maxQuantity) {
        if (maxQuantity <= 0) {
            throw new IllegalArgumentException("maxQuantity must be positive");
        }
        this.maxQuantity = maxQuantity;
    }

    @Override
    public RiskResult check(OrderRequest order) {
        if (order.quantity() > maxQuantity) {
            return RiskResult.fail("Max quantity exceeded: " + order.quantity() + " > " + maxQuantity);
        }
        return RiskResult.pass();
    }
}
