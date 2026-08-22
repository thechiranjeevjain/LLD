package com.chiranjeev.lld.oms;

final class OmsOrder {
    private final String id; private final String symbol;
    private long price; private long originalQuantity; private long filledQuantity;
    private OrderStatus status=OrderStatus.PENDING_NEW; private int version=1;
    OmsOrder(String id,String symbol,long price,long quantity){if(id==null||id.isBlank()||symbol==null||symbol.isBlank()||price<=0||quantity<=0)throw new IllegalArgumentException("invalid order");this.id=id;this.symbol=symbol;this.price=price;this.originalQuantity=quantity;}
    void acknowledgeNew(){require(OrderStatus.PENDING_NEW);status=OrderStatus.NEW;version++;}
    void rejectNew(){require(OrderStatus.PENDING_NEW);status=OrderStatus.REJECTED;version++;}
    void fill(long quantity){
        if(status!=OrderStatus.NEW&&status!=OrderStatus.PARTIALLY_FILLED&&status!=OrderStatus.PENDING_CANCEL&&status!=OrderStatus.PENDING_REPLACE)invalid("fill");
        if(quantity<=0||quantity>remaining())throw new IllegalArgumentException("invalid fill quantity");
        filledQuantity+=quantity; status=remaining()==0?OrderStatus.FILLED:status==OrderStatus.PENDING_CANCEL||status==OrderStatus.PENDING_REPLACE?status:OrderStatus.PARTIALLY_FILLED; version++;
    }
    void requestCancel(){requireLive("cancel");status=OrderStatus.PENDING_CANCEL;version++;}
    void acknowledgeCancel(){require(OrderStatus.PENDING_CANCEL);status=OrderStatus.CANCELLED;version++;}
    void rejectCancel(){require(OrderStatus.PENDING_CANCEL);status=filledQuantity==0?OrderStatus.NEW:OrderStatus.PARTIALLY_FILLED;version++;}
    void requestReplace(){requireLive("replace");status=OrderStatus.PENDING_REPLACE;version++;}
    void acknowledgeReplace(long newPrice,long newTotalQuantity){
        require(OrderStatus.PENDING_REPLACE); if(newPrice<=0||newTotalQuantity<filledQuantity)throw new IllegalArgumentException("replacement cannot be below filled quantity");
        price=newPrice;originalQuantity=newTotalQuantity;status=remaining()==0?OrderStatus.FILLED:filledQuantity==0?OrderStatus.NEW:OrderStatus.PARTIALLY_FILLED;version++;
    }
    void rejectReplace(){require(OrderStatus.PENDING_REPLACE);status=filledQuantity==0?OrderStatus.NEW:OrderStatus.PARTIALLY_FILLED;version++;}
    OrderSnapshot snapshot(){return new OrderSnapshot(id,symbol,price,originalQuantity,filledQuantity,remaining(),status,version);}
    private long remaining(){return originalQuantity-filledQuantity;}
    private void requireLive(String action){if(status!=OrderStatus.NEW&&status!=OrderStatus.PARTIALLY_FILLED)invalid(action);}
    private void require(OrderStatus expected){if(status!=expected)throw new InvalidTransitionException("expected "+expected+" but was "+status);}
    private void invalid(String action){throw new InvalidTransitionException("cannot "+action+" while "+status);}
}
