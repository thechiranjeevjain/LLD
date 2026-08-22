package com.chiranjeev.lld.risk;
public final class RiskEngineDemo {
    private RiskEngineDemo(){}
    public static void main(String[] args){
        RiskLimits limits=new RiskLimits(1_000,20_000_000,2_000,500);
        System.out.println(RiskEngine.standard().evaluate(new OrderRequest("ACC-1","AAPL",Side.BUY,100,10_100),new RiskContext(250,10_000,limits,false)));
    }
}
