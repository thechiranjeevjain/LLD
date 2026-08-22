package com.chiranjeev.lld.matching;

public final class MatchingEngineDemo {
    private MatchingEngineDemo() { }
    public static void main(String[] args) {
        MatchingEngine engine = new MatchingEngine();
        engine.submit(Order.limit("S1", Side.SELL, 10_100, 5));
        engine.submit(Order.limit("S2", Side.SELL, 10_100, 3));
        System.out.println(engine.submit(Order.market("B1", Side.BUY, 7)));
        System.out.printf("bestBid=%s bestAsk=%s%n", engine.bestBid(), engine.bestAsk());
    }
}
