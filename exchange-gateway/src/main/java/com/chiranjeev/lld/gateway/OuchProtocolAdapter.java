package com.chiranjeev.lld.gateway;
import java.util.*;
public final class OuchProtocolAdapter implements ProtocolAdapter {
    public String name(){return "OUCH-LIKE";}
    public String encodeNewOrder(InternalOrder o){return "O,"+o.id()+","+(o.side()==InternalOrder.Side.BUY?"B":"S")+","+o.symbol()+","+o.quantity()+","+o.price();}
    public VenueEvent decodeEvent(String wire){String[] p=wire.split(",",-1);if(p.length<3)throw new IllegalArgumentException("bad OUCH-like message");VenueEvent.Type t=switch(p[0]){case"A"->VenueEvent.Type.ACKNOWLEDGED;case"E"->VenueEvent.Type.PARTIAL_FILL;case"F"->VenueEvent.Type.FILLED;case"C"->VenueEvent.Type.CANCELLED;case"J"->VenueEvent.Type.REJECTED;default->throw new IllegalArgumentException("unknown type "+p[0]);};long q=p.length>3&&!p[3].isBlank()?Long.parseLong(p[3]):0;long price=p.length>4&&!p[4].isBlank()?Long.parseLong(p[4]):0;String reason=p.length>5?p[5]:"";return new VenueEvent(p[1],t,q,price,reason);}
}
