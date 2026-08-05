package org.chijai.orderbook.model;

public record PriceLevel(long price, long totalQuantity, int orderCount) {
    public PriceLevel {
        if (price <= 0) {
            throw new IllegalArgumentException("price must be positive");
        }
        if (totalQuantity < 0) {
            throw new IllegalArgumentException("total quantity must not be negative");
        }
        if (orderCount < 0) {
            throw new IllegalArgumentException("order count must not be negative");
        }
    }
}
