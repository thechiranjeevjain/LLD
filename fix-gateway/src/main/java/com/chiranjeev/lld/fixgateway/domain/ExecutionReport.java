package com.chiranjeev.lld.fixgateway.domain;

public record ExecutionReport(
        String orderId,
        String executionId,
        String clientOrderId,
        OrderStatus status,
        ExecType execType,
        String symbol,
        Side side,
        int quantity,
        String text
) {
    public static ExecutionReport accepted(OrderRequest order, String orderId, String executionId) {
        return new ExecutionReport(
                orderId,
                executionId,
                order.clientOrderId(),
                OrderStatus.NEW,
                ExecType.NEW,
                order.symbol(),
                order.side(),
                order.quantity(),
                "Accepted"
        );
    }

    public static ExecutionReport rejected(OrderRequest order, String reason) {
        return new ExecutionReport(
                "NONE",
                "NONE",
                order.clientOrderId(),
                OrderStatus.REJECTED,
                ExecType.REJECTED,
                order.symbol(),
                order.side(),
                order.quantity(),
                reason
        );
    }
}

