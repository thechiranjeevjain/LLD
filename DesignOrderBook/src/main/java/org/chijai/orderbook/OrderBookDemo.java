package org.chijai.orderbook;

import org.chijai.orderbook.engine.MatchingEngine;
import org.chijai.orderbook.model.ExecutionReport;
import org.chijai.orderbook.model.OrderBookSnapshot;
import org.chijai.orderbook.model.OrderRequest;
import org.chijai.orderbook.model.Side;

import java.util.List;

public final class OrderBookDemo {
    private OrderBookDemo() {
    }

    public static void main(String[] args) {
        MatchingEngine engine = new MatchingEngine();
        List<ExecutionReport> reports = List.of(
                engine.place(OrderRequest.limit("B-1", "AAPL", Side.BUY, 100_00, 10)),
                engine.place(OrderRequest.limit("B-2", "AAPL", Side.BUY, 101_00, 5)),
                engine.place(OrderRequest.limit("S-1", "AAPL", Side.SELL, 99_00, 12)),
                engine.place(OrderRequest.limit("S-2", "AAPL", Side.SELL, 102_00, 3)),
                engine.place(OrderRequest.market("B-3", "AAPL", Side.BUY, 2))
        );

        reports.forEach(System.out::println);

        OrderBookSnapshot snapshot = engine.snapshot("AAPL", 5);
        System.out.println("Snapshot: " + snapshot);
    }
}
