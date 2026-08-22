package com.chiranjeev.lld.risk;
import java.util.Optional;
public final class ExposureRule implements RiskRule {
    public String name(){return "MAX_EXPOSURE";}
    public Optional<Violation> evaluate(OrderRequest o,RiskContext c){long projected=Math.addExact(c.currentExposure(),o.signedQuantity()); return Math.abs(projected)>c.limits().maxAbsoluteExposure()?Optional.of(new Violation(name(),"projected exposure="+projected)):Optional.empty();}
}
