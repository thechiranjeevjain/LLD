package org.chijai.orderbook.model;

public final class Order {
    private final String orderId;
    private final String symbol;
    private final Side side;
    private final OrderType type;
    private final long price;
    private final long originalQuantity;
    private final long sequenceNumber;
    private long remainingQuantity;
    private OrderStatus status;

    private Order(OrderRequest request, long sequenceNumber) {
        this.orderId = request.orderId();
        this.symbol = request.symbol();
        this.side = request.side();
        this.type = request.type();
        this.price = request.price();
        this.originalQuantity = request.quantity();
        this.remainingQuantity = request.quantity();
        this.sequenceNumber = sequenceNumber;
        this.status = OrderStatus.ACCEPTED;
    }

    public static Order from(OrderRequest request, long sequenceNumber) {
        return new Order(request, sequenceNumber);
    }

    public void fill(long quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("fill quantity must be positive");
        }
        if (quantity > remainingQuantity) {
            throw new IllegalArgumentException("fill quantity exceeds remaining quantity");
        }

        remainingQuantity -= quantity;
        status = remainingQuantity == 0 ? OrderStatus.FILLED : OrderStatus.PARTIALLY_FILLED;
    }

    public void cancel() {
        if (remainingQuantity > 0) {
            status = OrderStatus.CANCELLED;
        }
    }

    public boolean isOpen() {
        return remainingQuantity > 0
                && (status == OrderStatus.ACCEPTED || status == OrderStatus.PARTIALLY_FILLED);
    }

    public long filledQuantity() {
        return originalQuantity - remainingQuantity;
    }

    public String orderId() {
        return orderId;
    }

    public String symbol() {
        return symbol;
    }

    public Side side() {
        return side;
    }

    public OrderType type() {
        return type;
    }

    public long price() {
        return price;
    }

    public long originalQuantity() {
        return originalQuantity;
    }

    public long remainingQuantity() {
        return remainingQuantity;
    }

    public long sequenceNumber() {
        return sequenceNumber;
    }

    public OrderStatus status() {
        return status;
    }
}
