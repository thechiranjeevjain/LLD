package com.chiranjeev.lld.risk;
import java.util.Optional;
public interface RiskRule { String name(); Optional<Violation> evaluate(OrderRequest order, RiskContext context); }
