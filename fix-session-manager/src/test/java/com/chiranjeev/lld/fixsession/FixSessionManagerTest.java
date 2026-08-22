package com.chiranjeev.lld.fixsession;
import org.junit.jupiter.api.Test;
import java.time.*;
import static org.junit.jupiter.api.Assertions.*;

class FixSessionManagerTest {
    private final Instant now=Instant.parse("2026-08-22T00:00:00Z");
    @Test void logonAndHeartbeatUsePersistentSequences(){InMemorySessionStore store=new InMemorySessionStore();FixSessionManager s=new FixSessionManager(store,Duration.ofSeconds(30));assertEquals(1,s.onInbound(FixMessage.inbound(1,MessageType.LOGON),now).get(0).message().sequence());assertEquals(2,s.onTimer(now.plusSeconds(31)).orElseThrow().message().sequence());}
    @Test void gapRequestsMissingRangeWithoutAdvancingInbound(){FixSessionManager s=loggedOn(new InMemorySessionStore());SessionAction a=s.onInbound(FixMessage.inbound(3,MessageType.NEW_ORDER),now).get(0);assertEquals(MessageType.RESEND_REQUEST,a.message().type());assertEquals(2,a.message().beginSequence());assertEquals(2,a.message().endSequence());assertEquals(SessionAction.Type.DELIVER,s.onInbound(FixMessage.inbound(2,MessageType.NEW_ORDER),now).get(0).type());}
    @Test void possibleDuplicateBelowExpectedIsIgnored(){FixSessionManager s=loggedOn(new InMemorySessionStore());s.onInbound(FixMessage.inbound(2,MessageType.NEW_ORDER),now);assertEquals(SessionAction.Type.DUPLICATE_IGNORED,s.onInbound(FixMessage.inbound(2,MessageType.NEW_ORDER).asDuplicate(),now).get(0).type());}
    @Test void resendRequestReplaysStoredOutboundAsPossDup(){InMemorySessionStore store=new InMemorySessionStore();FixSessionManager s=loggedOn(store);s.sendApplication("one",now);s.sendApplication("two",now);var replay=s.onInbound(FixMessage.resendRequest(2,2,3),now);assertEquals(2,replay.size());assertTrue(replay.get(0).message().possibleDuplicate());}
    @Test void reconnectRetainsSequenceState(){InMemorySessionStore store=new InMemorySessionStore();FixSessionManager first=loggedOn(store);first.onTransportDisconnected();FixSessionManager recovered=new FixSessionManager(store,Duration.ofSeconds(30));assertEquals(2,recovered.onInbound(FixMessage.inbound(2,MessageType.LOGON),now).get(0).message().sequence());}
    private FixSessionManager loggedOn(InMemorySessionStore store){FixSessionManager s=new FixSessionManager(store,Duration.ofSeconds(30));s.onInbound(FixMessage.inbound(1,MessageType.LOGON),now);return s;}
}
