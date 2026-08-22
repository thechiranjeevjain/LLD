package com.chiranjeev.lld.fixsession;
import java.util.List;
public interface SessionStore { int expectedInbound(); int nextOutbound(); void advanceInbound(); int claimOutbound(); void saveOutbound(FixMessage message); List<FixMessage> outboundRange(int begin,int end); }
