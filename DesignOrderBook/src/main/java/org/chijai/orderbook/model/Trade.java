package org.chijai.orderbook.model;

public record Trade(
        String symbol,
        String takerOrderId,
        String makerOrderId,
        Side takerSide,
        long price,
        long quantity,
        long sequenceNumber
) {
    public Trade {
        if (price <= 0) {
            throw new IllegalArgumentException("trade price must be positive");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("trade quantity must be positive");
        }
    }
}
