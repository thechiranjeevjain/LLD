package com.chiranjeev.lld.fixsession;
public record SessionAction(Type type,FixMessage message,String detail) { public enum Type { DELIVER, SEND, REPLAY, DUPLICATE_IGNORED, DISCONNECT } }
