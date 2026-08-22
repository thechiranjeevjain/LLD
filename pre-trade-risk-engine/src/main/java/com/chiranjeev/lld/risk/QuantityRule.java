package com.chiranjeev.lld.risk;
import java.util.Optional;
public final class QuantityRule implements RiskRule {
    public String name(){return "MAX_QUANTITY";}
    public Optional<Violation> evaluate(OrderRequest o,RiskContext c){return o.quantity()>c.limits().maxQuantity()?Optional.of(new Violation(name(),"quantity exceeds limit")):Optional.empty();}
}
