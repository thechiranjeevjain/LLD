package com.chiranjeev.lld.oms;

import java.util.*;
import java.util.function.Consumer;

public final class OrderManagementSystem {
    private final Map<String,OmsOrder> orders=new HashMap<>(); private final List<OrderEvent> events=new ArrayList<>(); private long sequence=1;
    public synchronized OrderSnapshot submit(String id,String symbol,long price,long quantity){if(orders.containsKey(id))throw new IllegalArgumentException("duplicate order id");OmsOrder o=new OmsOrder(id,symbol,price,quantity);orders.put(id,o);record(o,"SUBMITTED","");return o.snapshot();}
    public synchronized OrderSnapshot acknowledgeNew(String id){return mutate(id,"NEW_ACK","",OmsOrder::acknowledgeNew);}
    public synchronized OrderSnapshot rejectNew(String id,String reason){return mutate(id,"NEW_REJECT",reason,OmsOrder::rejectNew);}
    public synchronized OrderSnapshot recordFill(String id,long quantity,String executionId){return mutate(id,"FILL",executionId,o->o.fill(quantity));}
    public synchronized OrderSnapshot requestCancel(String id){return mutate(id,"CANCEL_REQUEST","",OmsOrder::requestCancel);}
    public synchronized OrderSnapshot acknowledgeCancel(String id){return mutate(id,"CANCEL_ACK","",OmsOrder::acknowledgeCancel);}
    public synchronized OrderSnapshot rejectCancel(String id,String reason){return mutate(id,"CANCEL_REJECT",reason,OmsOrder::rejectCancel);}
    public synchronized OrderSnapshot requestReplace(String id){return mutate(id,"REPLACE_REQUEST","",OmsOrder::requestReplace);}
    public synchronized OrderSnapshot acknowledgeReplace(String id,long price,long totalQuantity){return mutate(id,"REPLACE_ACK","",o->o.acknowledgeReplace(price,totalQuantity));}
    public synchronized OrderSnapshot rejectReplace(String id,String reason){return mutate(id,"REPLACE_REJECT",reason,OmsOrder::rejectReplace);}
    public synchronized OrderSnapshot find(String id){return order(id).snapshot();}
    public synchronized List<OrderEvent> events(){return List.copyOf(events);}
    private OrderSnapshot mutate(String id,String type,String detail,Consumer<OmsOrder> action){OmsOrder o=order(id);action.accept(o);record(o,type,detail);return o.snapshot();}
    private OmsOrder order(String id){OmsOrder o=orders.get(id);if(o==null)throw new NoSuchElementException("unknown order "+id);return o;}
    private void record(OmsOrder o,String type,String detail){OrderSnapshot s=o.snapshot();events.add(new OrderEvent(sequence++,s.orderId(),type,s.status(),detail));}
}
