package com.chiranjeev.lld.gateway;
public record InternalOrder(String id,String symbol,Side side,long quantity,long price) { public InternalOrder {if(id==null||id.isBlank()||symbol==null||symbol.isBlank()||side==null||quantity<=0||price<=0)throw new IllegalArgumentException("invalid order");} public enum Side{BUY,SELL} }
