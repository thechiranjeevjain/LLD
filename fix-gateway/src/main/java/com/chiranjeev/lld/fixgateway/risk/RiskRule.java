package com.chiranjeev.lld.fixgateway.risk;

import com.chiranjeev.lld.fixgateway.domain.OrderRequest;

public interface RiskRule {
    RiskResult check(OrderRequest order);
}

