package com.chiranjeev.lld.gateway;
public interface ProtocolAdapter { String name(); String encodeNewOrder(InternalOrder order); VenueEvent decodeEvent(String wireMessage); }
