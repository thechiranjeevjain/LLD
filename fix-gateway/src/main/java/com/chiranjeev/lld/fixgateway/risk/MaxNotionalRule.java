package com.chiranjeev.lld.fixgateway.risk;

import com.chiranjeev.lld.fixgateway.domain.OrderRequest;

import java.math.BigDecimal;

public final class MaxNotionalRule implements RiskRule {
    private final BigDecimal maxNotional;

    public MaxNotionalRule(BigDecimal maxNotional) {
        if (maxNotional == null || maxNotional.signum() <= 0) {
            throw new IllegalArgumentException("maxNotional must be positive");
        }
        this.maxNotional = maxNotional;
    }

    @Override
    public RiskResult check(OrderRequest order) {
        if (order.price() == null) {
            return RiskResult.pass();
        }
        if (order.notionalValue().compareTo(maxNotional) > 0) {
            return RiskResult.fail("Max notional exceeded: " + order.notionalValue() + " > " + maxNotional);
        }
        return RiskResult.pass();
    }
}
