package com.chiranjeev.lld.fixgateway.gateway;

import com.chiranjeev.lld.fixgateway.fix.FixMessage;
import com.chiranjeev.lld.fixgateway.fix.FixParser;
import com.chiranjeev.lld.fixgateway.fix.FixSerializer;
import com.chiranjeev.lld.fixgateway.fix.FixTags;
import com.chiranjeev.lld.fixgateway.fix.SessionEndpoint;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FixGatewayTest {
    private static final Clock FIXED_CLOCK = Clock.fixed(Instant.parse("2026-08-05T12:00:00Z"), ZoneOffset.UTC);

    private final FixParser parser = new FixParser();
    private final FixSerializer serializer = new FixSerializer(FIXED_CLOCK);
    private final SessionEndpoint clientEndpoint = new SessionEndpoint("CLIENT1", "FIX-GW");

    @Test
    void acceptsNewOrderAfterLogon() {
        FixGateway gateway = FixGateway.defaultGateway("FIX-GW");
        gateway.onMessage(inbound(1, FixMessage.builder(FixTags.MSG_TYPE_LOGON)
                .put(FixTags.ENCRYPT_METHOD, "0")
                .put(FixTags.HEART_BT_INT, "30")
                .build()));

        FixMessage executionReport = parser.parse(gateway.onMessage(inbound(2, newOrder("ORD-1", 100, new BigDecimal("175.25")))));

        assertEquals(FixTags.MSG_TYPE_EXECUTION_REPORT, executionReport.require(FixTags.MSG_TYPE));
        assertEquals("0", executionReport.require(FixTags.ORD_STATUS));
        assertEquals("Accepted", executionReport.require(FixTags.TEXT));
    }

    @Test
    void rejectsDuplicateClientOrderId() {
        FixGateway gateway = FixGateway.defaultGateway("FIX-GW");
        gateway.onMessage(inbound(1, FixMessage.builder(FixTags.MSG_TYPE_LOGON)
                .put(FixTags.ENCRYPT_METHOD, "0")
                .put(FixTags.HEART_BT_INT, "30")
                .build()));
        gateway.onMessage(inbound(2, newOrder("ORD-1", 100, new BigDecimal("175.25"))));

        FixMessage executionReport = parser.parse(gateway.onMessage(inbound(3, newOrder("ORD-1", 100, new BigDecimal("175.25")))));

        assertEquals("8", executionReport.require(FixTags.ORD_STATUS));
        assertEquals("Duplicate ClOrdID: ORD-1", executionReport.require(FixTags.TEXT));
    }

    @Test
    void rejectsMaxNotionalBreach() {
        FixGateway gateway = FixGateway.defaultGateway("FIX-GW");
        gateway.onMessage(inbound(1, FixMessage.builder(FixTags.MSG_TYPE_LOGON)
                .put(FixTags.ENCRYPT_METHOD, "0")
                .put(FixTags.HEART_BT_INT, "30")
                .build()));

        FixMessage executionReport = parser.parse(gateway.onMessage(inbound(2, newOrder("ORD-2", 100_000, new BigDecimal("420.00")))));

        assertEquals("8", executionReport.require(FixTags.ORD_STATUS));
        assertEquals("Max notional exceeded: 42000000.00 > 10000000.00", executionReport.require(FixTags.TEXT));
    }

    @Test
    void rejectsMessagesWithSequenceGap() {
        FixGateway gateway = FixGateway.defaultGateway("FIX-GW");

        assertThrows(SequenceException.class, () -> gateway.onMessage(inbound(2, FixMessage.builder(FixTags.MSG_TYPE_LOGON)
                .put(FixTags.ENCRYPT_METHOD, "0")
                .put(FixTags.HEART_BT_INT, "30")
                .build())));
    }

    private String inbound(int sequenceNumber, FixMessage message) {
        return serializer.serialize(clientEndpoint, sequenceNumber, message);
    }

    private FixMessage newOrder(String clOrdId, int quantity, BigDecimal price) {
        return FixMessage.builder(FixTags.MSG_TYPE_NEW_ORDER_SINGLE)
                .put(FixTags.CL_ORD_ID, clOrdId)
                .put(FixTags.SYMBOL, "AAPL")
                .put(FixTags.SIDE, "1")
                .put(FixTags.ORDER_QTY, quantity)
                .put(FixTags.ORD_TYPE, "2")
                .put(FixTags.PRICE, price)
                .put(FixTags.TIME_IN_FORCE, "0")
                .build();
    }
}

