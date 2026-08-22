package com.chiranjeev.lld.oms;
public record OrderSnapshot(String orderId,String symbol,long price,long originalQuantity,long filledQuantity,long remainingQuantity,OrderStatus status,int version) { }
