package com.chiranjeev.lld.gateway;
public record VenueEvent(String orderId,Type type,long lastQuantity,long lastPrice,String reason) { public enum Type{ACKNOWLEDGED,PARTIAL_FILL,FILLED,CANCELLED,REJECTED} }
