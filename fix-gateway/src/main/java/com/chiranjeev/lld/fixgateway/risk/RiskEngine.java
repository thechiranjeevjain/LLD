package com.chiranjeev.lld.fixgateway.risk;

import com.chiranjeev.lld.fixgateway.domain.OrderRequest;

import java.util.List;

public final class RiskEngine {
    private final List<RiskRule> rules;

    public RiskEngine(List<RiskRule> rules) {
        this.rules = List.copyOf(rules);
    }

    public RiskResult evaluate(OrderRequest order) {
        for (RiskRule rule : rules) {
            RiskResult result = rule.check(order);
            if (!result.accepted()) {
                return result;
            }
        }
        return RiskResult.pass();
    }
}
