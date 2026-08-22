package com.chiranjeev.lld.risk;
import java.util.Optional;
public final class PriceDeviationRule implements RiskRule {
    public String name(){return "PRICE_DEVIATION";}
    public Optional<Violation> evaluate(OrderRequest o,RiskContext c){
        if(c.referencePrice()<=0)return Optional.of(new Violation(name(),"reference price unavailable"));
        long deviationBps=Math.abs(o.limitPrice()-c.referencePrice())*10_000/c.referencePrice();
        return deviationBps>c.limits().maxPriceDeviationBps()?Optional.of(new Violation(name(),"deviation="+deviationBps+"bps")):Optional.empty();
    }
}
