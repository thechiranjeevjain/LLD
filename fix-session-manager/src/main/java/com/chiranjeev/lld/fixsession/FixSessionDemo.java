package com.chiranjeev.lld.fixsession;
import java.time.*;
public final class FixSessionDemo {private FixSessionDemo(){}public static void main(String[] args){FixSessionManager s=new FixSessionManager(new InMemorySessionStore(),Duration.ofSeconds(30));Instant now=Instant.parse("2026-08-22T00:00:00Z");System.out.println(s.onInbound(FixMessage.inbound(1,MessageType.LOGON),now));System.out.println(s.sendApplication("11=ORDER-1",now));System.out.println(s.onTimer(now.plusSeconds(31)));}}
