package com.chiranjeev.lld.matching;

public record Trade(long sequence, String makerOrderId, String takerOrderId, long price, long quantity) { }
