package com.chiranjeev.lld.fixsession;
import java.util.*;
public final class InMemorySessionStore implements SessionStore {
    private int inbound=1,outbound=1; private final NavigableMap<Integer,FixMessage> sent=new TreeMap<>();
    public synchronized int expectedInbound(){return inbound;} public synchronized int nextOutbound(){return outbound;}
    public synchronized void advanceInbound(){inbound++;} public synchronized int claimOutbound(){return outbound++;}
    public synchronized void saveOutbound(FixMessage m){sent.put(m.sequence(),m);} public synchronized List<FixMessage> outboundRange(int b,int e){return List.copyOf(sent.subMap(b,true,e,true).values());}
}
