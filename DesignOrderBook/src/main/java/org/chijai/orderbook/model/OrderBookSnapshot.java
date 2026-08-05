package org.chijai.orderbook.model;

import java.util.List;

public record OrderBookSnapshot(
        String symbol,
        Long bestBid,
        Long bestAsk,
        List<PriceLevel> bids,
        List<PriceLevel> asks
) {
    public OrderBookSnapshot {
        bids = List.copyOf(bids);
        asks = List.copyOf(asks);
    }
}
