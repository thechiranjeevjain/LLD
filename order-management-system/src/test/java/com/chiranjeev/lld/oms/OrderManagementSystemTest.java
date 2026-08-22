package com.chiranjeev.lld.oms;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class OrderManagementSystemTest {
    @Test void tracksAcknowledgementPartialFillAndCancel(){OrderManagementSystem oms=new OrderManagementSystem();oms.submit("O1","AAPL",100,10);oms.acknowledgeNew("O1");oms.recordFill("O1",4,"E1");oms.requestCancel("O1");OrderSnapshot s=oms.acknowledgeCancel("O1");assertEquals(OrderStatus.CANCELLED,s.status());assertEquals(4,s.filledQuantity());assertEquals(6,s.remainingQuantity());assertEquals(5,oms.events().size());}
    @Test void replacementUpdatesLeavesAndPreservesFills(){OrderManagementSystem oms=new OrderManagementSystem();oms.submit("O1","AAPL",100,10);oms.acknowledgeNew("O1");oms.recordFill("O1",3,"E1");oms.requestReplace("O1");OrderSnapshot s=oms.acknowledgeReplace("O1",101,12);assertEquals(OrderStatus.PARTIALLY_FILLED,s.status());assertEquals(9,s.remainingQuantity());assertEquals(101,s.price());}
    @Test void fillCanCompleteWhileCancelPending(){OrderManagementSystem oms=new OrderManagementSystem();oms.submit("O1","AAPL",100,2);oms.acknowledgeNew("O1");oms.requestCancel("O1");assertEquals(OrderStatus.FILLED,oms.recordFill("O1",2,"E1").status());assertThrows(InvalidTransitionException.class,()->oms.acknowledgeCancel("O1"));}
    @Test void replaceRejectAfterRacingFillReturnsToPartial(){OrderManagementSystem oms=new OrderManagementSystem();oms.submit("O1","AAPL",100,3);oms.acknowledgeNew("O1");oms.requestReplace("O1");oms.recordFill("O1",1,"E1");assertEquals(OrderStatus.PARTIALLY_FILLED,oms.rejectReplace("O1","too late").status());}
    @Test void invalidTransitionsFailFast(){OrderManagementSystem oms=new OrderManagementSystem();oms.submit("O1","AAPL",100,2);assertThrows(InvalidTransitionException.class,()->oms.requestCancel("O1"));oms.rejectNew("O1","venue down");assertThrows(InvalidTransitionException.class,()->oms.recordFill("O1",1,"E1"));}
}
