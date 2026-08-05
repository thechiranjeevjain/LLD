package com.chiranjeev.lld.fixgateway.domain;

import java.math.BigDecimal;
import java.util.Objects;

public record OrderRequest(
        String clientOrderId,
        String clientCompId,
        String symbol,
        Side side,
        int quantity,
        OrderType orderType,
        BigDecimal price,
        TimeInForce timeInForce
) {
    public OrderRequest {
        clientOrderId = requireText(clientOrderId, "clientOrderId");
        clientCompId = requireText(clientCompId, "clientCompId");
        symbol = requireText(symbol, "symbol");
        Objects.requireNonNull(side, "side");
        Objects.requireNonNull(orderType, "orderType");
        Objects.requireNonNull(timeInForce, "timeInForce");

        if (quantity <= 0) {
            throw new IllegalArgumentException("quantity must be positive");
        }

        if (orderType == OrderType.LIMIT) {
            if (price == null || price.signum() <= 0) {
                throw new IllegalArgumentException("limit orders require a positive price");
            }
        } else if (price != null && price.signum() < 0) {
            throw new IllegalArgumentException("price cannot be negative");
        }
    }

    public BigDecimal notionalValue() {
        BigDecimal effectivePrice = price == null ? BigDecimal.ZERO : price;
        return effectivePrice.multiply(BigDecimal.valueOf(quantity));
    }

    private static String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " is required");
        }
        return value.trim();
    }
}

