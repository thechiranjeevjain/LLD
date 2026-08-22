package com.chiranjeev.lld.fixsession;

import java.time.*;
import java.util.*;

public final class FixSessionManager {
    private final SessionStore store; private final Duration heartbeatInterval; private SessionState state=SessionState.DISCONNECTED; private Instant lastSent=Instant.EPOCH;
    public FixSessionManager(SessionStore store,Duration heartbeatInterval){this.store=Objects.requireNonNull(store);this.heartbeatInterval=Objects.requireNonNull(heartbeatInterval);}

    public synchronized List<SessionAction> onInbound(FixMessage message,Instant now){
        int expected=store.expectedInbound();
        if(message.sequence()<expected){
            if(message.possibleDuplicate())return List.of(new SessionAction(SessionAction.Type.DUPLICATE_IGNORED,message,"already processed"));
            throw new IllegalStateException("sequence too low: expected "+expected);
        }
        if(message.sequence()>expected){return List.of(send(MessageType.RESEND_REQUEST,expected,message.sequence()-1,"",now));}
        store.advanceInbound();
        if(message.type()==MessageType.LOGON){state=SessionState.LOGGED_ON;return List.of(send(MessageType.LOGON,0,0,"ack",now));}
        requireLoggedOn();
        if(message.type()==MessageType.RESEND_REQUEST){
            int end=message.endSequence()==0?store.nextOutbound()-1:message.endSequence();
            return store.outboundRange(message.beginSequence(),end).stream().map(m->new SessionAction(SessionAction.Type.REPLAY,m.asDuplicate(),"possDup replay")).toList();
        }
        if(message.type()==MessageType.LOGOUT){state=SessionState.DISCONNECTED;return List.of(new SessionAction(SessionAction.Type.DISCONNECT,message,"peer logout"));}
        return List.of(new SessionAction(SessionAction.Type.DELIVER,message,"application/session message"));
    }

    public synchronized Optional<SessionAction> onTimer(Instant now){
        if(state==SessionState.LOGGED_ON&&Duration.between(lastSent,now).compareTo(heartbeatInterval)>=0)return Optional.of(send(MessageType.HEARTBEAT,0,0,"",now));
        return Optional.empty();
    }
    public synchronized SessionAction sendApplication(String payload,Instant now){requireLoggedOn();return send(MessageType.NEW_ORDER,0,0,payload,now);}
    public synchronized void onTransportDisconnected(){state=SessionState.DISCONNECTED;}
    public synchronized SessionState state(){return state;}
    private SessionAction send(MessageType type,int begin,int end,String payload,Instant now){int seq=store.claimOutbound();FixMessage m=new FixMessage(seq,type,false,begin,end,payload);store.saveOutbound(m);lastSent=now;return new SessionAction(SessionAction.Type.SEND,m,"");}
    private void requireLoggedOn(){if(state!=SessionState.LOGGED_ON)throw new IllegalStateException("session is not logged on");}
}
