package com.chiranjeev.lld.risk;

import java.util.List;

public final class RiskEngine {
    private final List<RiskRule> rules;
    public RiskEngine(List<RiskRule> rules){this.rules=List.copyOf(rules);}
    public RiskDecision evaluate(OrderRequest order,RiskContext context){
        List<Violation> failures=rules.stream().map(rule->rule.evaluate(order,context)).flatMap(java.util.Optional::stream).toList();
        return new RiskDecision(failures.isEmpty(),failures);
    }
    public static RiskEngine standard(){return new RiskEngine(List.of(new KillSwitchRule(),new QuantityRule(),new NotionalRule(),new ExposureRule(),new PriceDeviationRule()));}
}
