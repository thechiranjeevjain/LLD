package com.chiranjeev.lld.matching;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MatchingEngineTest {
    @Test void marketOrderUsesBestPriceThenFifoAndPartiallyFillsMaker() {
        MatchingEngine engine = new MatchingEngine();
        engine.submit(Order.limit("S0", Side.SELL, 10_200, 2));
        engine.submit(Order.limit("S1", Side.SELL, 10_100, 3));
        engine.submit(Order.limit("S2", Side.SELL, 10_100, 4));
        MatchResult result = engine.submit(Order.market("B1", Side.BUY, 5));
        assertEquals(5, result.filledQuantity()); assertEquals(0, result.remainingQuantity());
        assertEquals("S1", result.trades().get(0).makerOrderId()); assertEquals("S2", result.trades().get(1).makerOrderId());
        assertEquals(10_100L, engine.bestAsk());
    }

    @Test void nonCrossingLimitRestsAndCrossingLimitTradesAtMakerPrice() {
        MatchingEngine engine = new MatchingEngine();
        MatchResult bid = engine.submit(Order.limit("B1", Side.BUY, 9_900, 5));
        assertTrue(bid.resting()); assertEquals(9_900L, engine.bestBid());
        MatchResult sell = engine.submit(Order.limit("S1", Side.SELL, 9_800, 2));
        assertEquals(9_900, sell.trades().get(0).price()); assertEquals(2, sell.filledQuantity());
        assertEquals(9_900L, engine.bestBid());
    }

    @Test void marketRemainderNeverRestsAndIdsAreUnique() {
        MatchingEngine engine = new MatchingEngine();
        MatchResult result = engine.submit(Order.market("B1", Side.BUY, 4));
        assertFalse(result.resting()); assertEquals(4, result.remainingQuantity());
        assertThrows(IllegalArgumentException.class, () -> engine.submit(Order.market("B1", Side.BUY, 1)));
    }
}
