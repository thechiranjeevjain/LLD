package com.chiranjeev.lld.gateway;
import java.util.*;
public final class FixProtocolAdapter implements ProtocolAdapter {
    public String name(){return "FIX";}
    public String encodeNewOrder(InternalOrder o){return "35=D|11="+o.id()+"|55="+o.symbol()+"|54="+(o.side()==InternalOrder.Side.BUY?"1":"2")+"|38="+o.quantity()+"|40=2|44="+o.price()+"|";}
    public VenueEvent decodeEvent(String wire){Map<String,String> f=fields(wire,"\\|");String status=req(f,"39");VenueEvent.Type type=switch(status){case"0"->VenueEvent.Type.ACKNOWLEDGED;case"1"->VenueEvent.Type.PARTIAL_FILL;case"2"->VenueEvent.Type.FILLED;case"4"->VenueEvent.Type.CANCELLED;case"8"->VenueEvent.Type.REJECTED;default->throw new IllegalArgumentException("unknown OrdStatus "+status);};return new VenueEvent(req(f,"11"),type,num(f,"32"),num(f,"31"),f.getOrDefault("58",""));}
    static Map<String,String> fields(String wire,String delimiter){Map<String,String> m=new HashMap<>();for(String token:wire.split(delimiter))if(token.contains("=")){String[] p=token.split("=",2);m.put(p[0],p[1]);}return m;}private static String req(Map<String,String>m,String k){String v=m.get(k);if(v==null)throw new IllegalArgumentException("missing "+k);return v;}private static long num(Map<String,String>m,String k){return Long.parseLong(m.getOrDefault(k,"0"));}
}
