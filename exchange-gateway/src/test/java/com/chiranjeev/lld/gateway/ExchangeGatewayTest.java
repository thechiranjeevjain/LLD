package com.chiranjeev.lld.gateway;
import org.junit.jupiter.api.Test;
import java.time.Instant;import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class ExchangeGatewayTest {
    @Test void fixAndOuchAdaptersTranslateOrdersAndEvents(){InternalOrder o=new InternalOrder("O1","AAPL",InternalOrder.Side.BUY,10,101);FixProtocolAdapter fix=new FixProtocolAdapter();assertTrue(fix.encodeNewOrder(o).contains("35=D|11=O1"));assertEquals(VenueEvent.Type.PARTIAL_FILL,fix.decodeEvent("11=O1|39=1|32=3|31=101|").type());OuchProtocolAdapter ouch=new OuchProtocolAdapter();assertEquals("O,O1,B,AAPL,10,101",ouch.encodeNewOrder(o));assertEquals(VenueEvent.Type.FILLED,ouch.decodeEvent("F,O1,,10,101,").type());}
    @Test void gatewayRoutesAndPublishesVenueEvents(){List<String> wire=new ArrayList<>();List<VenueEvent> events=new ArrayList<>();ExchangeGateway g=new ExchangeGateway(new FixProtocolAdapter(),()->true,wire::add,events::add);g.onConnected();assertEquals(SubmissionResult.Status.ROUTED,g.submit(new InternalOrder("O1","IBM",InternalOrder.Side.SELL,2,99)).status());g.onVenueMessage("11=O1|39=2|32=2|31=99|");assertEquals(1,wire.size());assertEquals(VenueEvent.Type.FILLED,events.get(0).type());}
    @Test void disconnectedOrdersQueueAndFlushAfterReconnect(){List<String> wire=new ArrayList<>();ExchangeGateway g=new ExchangeGateway(new OuchProtocolAdapter(),()->true,wire::add,e->{});assertEquals(SubmissionResult.Status.QUEUED_DISCONNECTED,g.submit(new InternalOrder("O1","IBM",InternalOrder.Side.BUY,2,99)).status());assertEquals(1,g.queuedCount());assertEquals(1,g.onConnected());assertEquals(0,g.queuedCount());assertEquals(1,wire.size());}
    @Test void throttleQueuesLiveSubmissionForLaterFlush(){List<String> wire=new ArrayList<>();boolean[] permit={false};ExchangeGateway g=new ExchangeGateway(new FixProtocolAdapter(),()->permit[0],wire::add,e->{});g.onConnected();assertEquals(SubmissionResult.Status.QUEUED_THROTTLED,g.submit(new InternalOrder("O1","IBM",InternalOrder.Side.BUY,2,99)).status());assertEquals(1,g.queuedCount());permit[0]=true;assertEquals(1,g.flush());assertEquals(1,wire.size());}
    @Test void tokenBucketRefillsWithInjectedClock(){Instant[] now={Instant.EPOCH};TokenBucketRateLimiter l=new TokenBucketRateLimiter(1,1,()->now[0]);assertTrue(l.tryAcquire());assertFalse(l.tryAcquire());now[0]=now[0].plusSeconds(1);assertTrue(l.tryAcquire());}
}
