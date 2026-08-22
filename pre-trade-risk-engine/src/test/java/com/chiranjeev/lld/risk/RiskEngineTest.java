package com.chiranjeev.lld.risk;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class RiskEngineTest {
    private final RiskLimits limits=new RiskLimits(100,1_000_000,150,500);
    @Test void acceptsOrderInsideAllLimits(){
        RiskDecision d=RiskEngine.standard().evaluate(new OrderRequest("A","IBM",Side.BUY,10,10_000),new RiskContext(20,10_100,limits,false));
        assertTrue(d.accepted()); assertTrue(d.violations().isEmpty());
    }
    @Test void reportsAllIndependentFailures(){
        RiskDecision d=RiskEngine.standard().evaluate(new OrderRequest("A","IBM",Side.BUY,200,20_000),new RiskContext(0,10_000,limits,true));
        assertFalse(d.accepted()); assertEquals(List.of("KILL_SWITCH","MAX_QUANTITY","MAX_NOTIONAL","MAX_EXPOSURE","PRICE_DEVIATION"),d.violations().stream().map(Violation::rule).toList());
    }
    @Test void sellOffsetsLongExposure(){
        RiskDecision d=RiskEngine.standard().evaluate(new OrderRequest("A","IBM",Side.SELL,100,10_000),new RiskContext(140,10_000,limits,false));
        assertTrue(d.accepted());
    }
    @Test void customRuleCanBePluggedIn(){
        RiskRule rejectAll=new RiskRule(){public String name(){return "CUSTOM";} public java.util.Optional<Violation> evaluate(OrderRequest o,RiskContext c){return java.util.Optional.of(new Violation(name(),"no"));}};
        assertEquals("CUSTOM",new RiskEngine(List.of(rejectAll)).evaluate(new OrderRequest("A","IBM",Side.BUY,1,1),new RiskContext(0,1,limits,false)).violations().get(0).rule());
    }
}
