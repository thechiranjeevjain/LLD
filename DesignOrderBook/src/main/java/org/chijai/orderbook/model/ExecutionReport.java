package org.chijai.orderbook.model;

import java.util.List;

public record ExecutionReport(
        String orderId,
        String symbol,
        Side side,
        OrderStatus status,
        long originalQuantity,
        long remainingQuantity,
        List<Trade> trades,
        String message
) {
    public ExecutionReport {
        trades = List.copyOf(trades);
    }

    public static ExecutionReport from(Order order, List<Trade> trades, String message) {
        return new ExecutionReport(
                order.orderId(),
                order.symbol(),
                order.side(),
                order.status(),
                order.originalQuantity(),
                order.remainingQuantity(),
                trades,
                message
        );
    }

    public static ExecutionReport rejected(OrderRequest request, String message) {
        return new ExecutionReport(
                request.orderId(),
                request.symbol(),
                request.side(),
                OrderStatus.REJECTED,
                request.quantity(),
                request.quantity(),
                List.of(),
                message
        );
    }

    public static ExecutionReport rejected(String orderId, String symbol, String message) {
        return new ExecutionReport(
                orderId,
                symbol,
                null,
                OrderStatus.REJECTED,
                0,
                0,
                List.of(),
                message
        );
    }

    public long filledQuantity() {
        return originalQuantity - remainingQuantity;
    }

    public boolean rejected() {
        return status == OrderStatus.REJECTED;
    }
}
