package com.chiranjeev.lld.oms;
public record OrderEvent(long sequence,String orderId,String type,OrderStatus status,String detail) { }
