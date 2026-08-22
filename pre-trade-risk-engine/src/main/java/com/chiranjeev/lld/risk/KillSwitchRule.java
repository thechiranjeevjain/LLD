package com.chiranjeev.lld.risk;
import java.util.Optional;
public final class KillSwitchRule implements RiskRule {
    public String name(){return "KILL_SWITCH";}
    public Optional<Violation> evaluate(OrderRequest o,RiskContext c){return c.killSwitchEnabled()?Optional.of(new Violation(name(),"trading disabled for account")):Optional.empty();}
}
