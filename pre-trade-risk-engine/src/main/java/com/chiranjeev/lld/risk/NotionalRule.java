package com.chiranjeev.lld.risk;
import java.util.Optional;
public final class NotionalRule implements RiskRule {
    public String name(){return "MAX_NOTIONAL";}
    public Optional<Violation> evaluate(OrderRequest o,RiskContext c){return o.notional()>c.limits().maxNotional()?Optional.of(new Violation(name(),"notional exceeds limit")):Optional.empty();}
}
