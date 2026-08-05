package com.chiranjeev.lld.fixgateway;

import com.chiranjeev.lld.fixgateway.fix.FixMessage;
import com.chiranjeev.lld.fixgateway.fix.FixSerializer;
import com.chiranjeev.lld.fixgateway.fix.FixTags;
import com.chiranjeev.lld.fixgateway.fix.SessionEndpoint;
import com.chiranjeev.lld.fixgateway.gateway.FixGateway;

import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicInteger;

public final class FixGatewayDemo {
    private FixGatewayDemo() {
    }

    public static void main(String[] args) {
        FixGateway gateway = FixGateway.defaultGateway("FIX-GW");
        FixSerializer serializer = new FixSerializer();
        SessionEndpoint clientEndpoint = new SessionEndpoint("CLIENT1", "FIX-GW");
        AtomicInteger clientSequence = new AtomicInteger(1);

        send("Logon", gateway, serializer, clientEndpoint, clientSequence,
                FixMessage.builder(FixTags.MSG_TYPE_LOGON)
                        .put(FixTags.ENCRYPT_METHOD, "0")
                        .put(FixTags.HEART_BT_INT, "30")
                        .build());

        send("Accepted limit order", gateway, serializer, clientEndpoint, clientSequence,
                newOrder("ORD-1", "AAPL", "1", 100, new BigDecimal("175.25")));

        send("Duplicate client order id", gateway, serializer, clientEndpoint, clientSequence,
                newOrder("ORD-1", "AAPL", "1", 100, new BigDecimal("175.25")));

        send("Max notional rejection", gateway, serializer, clientEndpoint, clientSequence,
                newOrder("ORD-2", "MSFT", "2", 100_000, new BigDecimal("420.00")));
    }

    private static FixMessage newOrder(String clOrdId, String symbol, String side, int quantity, BigDecimal price) {
        return FixMessage.builder(FixTags.MSG_TYPE_NEW_ORDER_SINGLE)
                .put(FixTags.CL_ORD_ID, clOrdId)
                .put(FixTags.SYMBOL, symbol)
                .put(FixTags.SIDE, side)
                .put(FixTags.ORDER_QTY, quantity)
                .put(FixTags.ORD_TYPE, "2")
                .put(FixTags.PRICE, price)
                .put(FixTags.TIME_IN_FORCE, "0")
                .build();
    }

    private static void send(
            String label,
            FixGateway gateway,
            FixSerializer serializer,
            SessionEndpoint endpoint,
            AtomicInteger clientSequence,
            FixMessage message
    ) {
        String inbound = serializer.serialize(endpoint, clientSequence.getAndIncrement(), message);
        String outbound = gateway.onMessage(inbound);

        System.out.println("== " + label + " ==");
        System.out.println(serializer.toPrintable(outbound));
        System.out.println();
    }
}

