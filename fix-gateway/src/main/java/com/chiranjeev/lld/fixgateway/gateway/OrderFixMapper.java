package com.chiranjeev.lld.fixgateway.gateway;

import com.chiranjeev.lld.fixgateway.domain.ExecutionReport;
import com.chiranjeev.lld.fixgateway.domain.OrderRequest;
import com.chiranjeev.lld.fixgateway.domain.OrderType;
import com.chiranjeev.lld.fixgateway.domain.Side;
import com.chiranjeev.lld.fixgateway.domain.TimeInForce;
import com.chiranjeev.lld.fixgateway.fix.FixMessage;
import com.chiranjeev.lld.fixgateway.fix.FixTags;

import java.math.BigDecimal;

public final class OrderFixMapper {
    public OrderRequest toOrderRequest(FixMessage message, String clientCompId) {
        OrderType orderType = OrderType.fromFixValue(message.require(FixTags.ORD_TYPE));
        BigDecimal price = message.get(FixTags.PRICE)
                .map(this::parsePrice)
                .orElse(null);

        return new OrderRequest(
                message.require(FixTags.CL_ORD_ID),
                clientCompId,
                message.require(FixTags.SYMBOL),
                Side.fromFixValue(message.require(FixTags.SIDE)),
                parseQuantity(message.require(FixTags.ORDER_QTY)),
                orderType,
                price,
                TimeInForce.fromFixValue(message.get(FixTags.TIME_IN_FORCE).orElse("0"))
        );
    }

    public FixMessage toExecutionReport(ExecutionReport report, OrderRequest order) {
        FixMessage.Builder builder = FixMessage.builder(FixTags.MSG_TYPE_EXECUTION_REPORT)
                .put(FixTags.ORDER_ID, report.orderId())
                .put(FixTags.EXEC_ID, report.executionId())
                .put(FixTags.CL_ORD_ID, report.clientOrderId())
                .put(FixTags.EXEC_TYPE, report.execType().fixValue())
                .put(FixTags.ORD_STATUS, report.status().fixValue())
                .put(FixTags.SYMBOL, report.symbol())
                .put(FixTags.SIDE, report.side().fixValue())
                .put(FixTags.ORDER_QTY, report.quantity())
                .put(FixTags.ORD_TYPE, order.orderType().fixValue())
                .put(FixTags.TEXT, report.text());

        if (order.price() != null) {
            builder.put(FixTags.PRICE, order.price());
        }
        builder.put(FixTags.TIME_IN_FORCE, order.timeInForce().fixValue());
        return builder.build();
    }

    private int parseQuantity(String value) {
        try {
            int quantity = Integer.parseInt(value);
            if (quantity <= 0) {
                throw new NumberFormatException("non-positive");
            }
            return quantity;
        } catch (NumberFormatException ex) {
            throw new FixValidationException("Invalid OrderQty: " + value, ex);
        }
    }

    private BigDecimal parsePrice(String value) {
        try {
            return new BigDecimal(value);
        } catch (NumberFormatException ex) {
            throw new FixValidationException("Invalid Price: " + value, ex);
        }
    }
}

