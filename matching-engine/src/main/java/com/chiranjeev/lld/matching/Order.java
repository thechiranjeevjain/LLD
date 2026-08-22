package com.chiranjeev.lld.matching;

import java.util.Objects;

public final class Order {
    private final String id;
    private final Side side;
    private final OrderType type;
    private final long price;
    private final long originalQuantity;
    private long remainingQuantity;

    private Order(String id, Side side, OrderType type, long price, long quantity) {
        this.id = requireText(id); this.side = Objects.requireNonNull(side); this.type = Objects.requireNonNull(type);
        if (quantity <= 0) throw new IllegalArgumentException("quantity must be positive");
        if (type == OrderType.LIMIT && price <= 0) throw new IllegalArgumentException("limit price must be positive");
        if (type == OrderType.MARKET && price != 0) throw new IllegalArgumentException("market price must be zero");
        this.price = price; this.originalQuantity = quantity; this.remainingQuantity = quantity;
    }

    public static Order limit(String id, Side side, long price, long quantity) { return new Order(id, side, OrderType.LIMIT, price, quantity); }
    public static Order market(String id, Side side, long quantity) { return new Order(id, side, OrderType.MARKET, 0, quantity); }
    public void fill(long quantity) { if (quantity <= 0 || quantity > remainingQuantity) throw new IllegalArgumentException("invalid fill"); remainingQuantity -= quantity; }
    public String id() { return id; } public Side side() { return side; } public OrderType type() { return type; }
    public long price() { return price; } public long originalQuantity() { return originalQuantity; } public long remainingQuantity() { return remainingQuantity; }
    private static String requireText(String value) { Objects.requireNonNull(value); if (value.isBlank()) throw new IllegalArgumentException("id is blank"); return value; }
}
