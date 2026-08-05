package org.chijai.orderbook.model;

import java.util.Locale;
import java.util.Objects;

public record OrderRequest(
        String orderId,
        String symbol,
        Side side,
        OrderType type,
        long price,
        long quantity,
        TimeInForce timeInForce
) {
    public OrderRequest {
        orderId = requireText(orderId, "orderId");
        symbol = requireText(symbol, "symbol").toUpperCase(Locale.ROOT);
        Objects.requireNonNull(side, "side is required");
        Objects.requireNonNull(type, "type is required");
        timeInForce = timeInForce == null ? defaultTimeInForce(type) : timeInForce;

        if (quantity <= 0) {
            throw new IllegalArgumentException("quantity must be positive");
        }
        if (type == OrderType.LIMIT && price <= 0) {
            throw new IllegalArgumentException("limit order price must be positive");
        }
        if (type == OrderType.MARKET && price != 0) {
            throw new IllegalArgumentException("market order price must be 0");
        }
        if (type == OrderType.MARKET && timeInForce == TimeInForce.GTC) {
            throw new IllegalArgumentException("market orders cannot be GTC because they never rest on the book");
        }
    }

    public static OrderRequest limit(String orderId, String symbol, Side side, long price, long quantity) {
        return limit(orderId, symbol, side, price, quantity, TimeInForce.GTC);
    }

    public static OrderRequest limit(
            String orderId,
            String symbol,
            Side side,
            long price,
            long quantity,
            TimeInForce timeInForce
    ) {
        return new OrderRequest(orderId, symbol, side, OrderType.LIMIT, price, quantity, timeInForce);
    }

    public static OrderRequest market(String orderId, String symbol, Side side, long quantity) {
        return new OrderRequest(orderId, symbol, side, OrderType.MARKET, 0, quantity, TimeInForce.IOC);
    }

    private static TimeInForce defaultTimeInForce(OrderType type) {
        return type == OrderType.MARKET ? TimeInForce.IOC : TimeInForce.GTC;
    }

    private static String requireText(String value, String fieldName) {
        Objects.requireNonNull(value, fieldName + " is required");
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return trimmed;
    }
}
