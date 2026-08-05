package org.chijai.orderbook.engine;

import org.chijai.orderbook.model.ExecutionReport;
import org.chijai.orderbook.model.OrderBookSnapshot;
import org.chijai.orderbook.model.OrderRequest;
import org.chijai.orderbook.model.OrderStatus;
import org.chijai.orderbook.model.Side;
import org.chijai.orderbook.model.TimeInForce;
import org.chijai.orderbook.model.Trade;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OrderBookTest {
    @Test
    void limitOrderRestsWhenBookHasNoMatch() {
        OrderBook book = new OrderBook("AAPL");

        ExecutionReport report = book.place(OrderRequest.limit("B-1", "AAPL", Side.BUY, 100_00, 10));

        assertEquals(OrderStatus.ACCEPTED, report.status());
        assertEquals(10, report.remainingQuantity());
        assertTrue(report.trades().isEmpty());

        OrderBookSnapshot snapshot = book.snapshot(5);
        assertEquals(100_00, snapshot.bestBid());
        assertNull(snapshot.bestAsk());
        assertEquals(1, snapshot.bids().size());
        assertEquals(10, snapshot.bids().get(0).totalQuantity());
        assertEquals(1, snapshot.bids().get(0).orderCount());
    }

    @Test
    void marketOrderConsumesBestPriceThenFifoWithinPriceLevel() {
        OrderBook book = new OrderBook("AAPL");
        book.place(OrderRequest.limit("B-1", "AAPL", Side.BUY, 100_00, 5));
        book.place(OrderRequest.limit("B-2", "AAPL", Side.BUY, 101_00, 2));
        book.place(OrderRequest.limit("B-3", "AAPL", Side.BUY, 101_00, 3));

        ExecutionReport report = book.place(OrderRequest.market("S-1", "AAPL", Side.SELL, 4));

        assertEquals(OrderStatus.FILLED, report.status());
        assertEquals(0, report.remainingQuantity());
        assertEquals(2, report.trades().size());

        Trade firstTrade = report.trades().get(0);
        assertEquals("B-2", firstTrade.makerOrderId());
        assertEquals(101_00, firstTrade.price());
        assertEquals(2, firstTrade.quantity());

        Trade secondTrade = report.trades().get(1);
        assertEquals("B-3", secondTrade.makerOrderId());
        assertEquals(101_00, secondTrade.price());
        assertEquals(2, secondTrade.quantity());

        OrderBookSnapshot snapshot = book.snapshot(5);
        assertEquals(101_00, snapshot.bestBid());
        assertEquals(1, snapshot.bids().get(0).totalQuantity());
        assertEquals(1, snapshot.bids().get(0).orderCount());
    }

    @Test
    void crossingLimitOrderTradesAtRestingOrderPrice() {
        OrderBook book = new OrderBook("AAPL");
        book.place(OrderRequest.limit("S-1", "AAPL", Side.SELL, 102_00, 4));

        ExecutionReport report = book.place(OrderRequest.limit("B-1", "AAPL", Side.BUY, 105_00, 3));

        assertEquals(OrderStatus.FILLED, report.status());
        assertEquals(1, report.trades().size());
        assertEquals(102_00, report.trades().get(0).price());
        assertEquals(3, report.trades().get(0).quantity());

        OrderBookSnapshot snapshot = book.snapshot(5);
        assertNull(snapshot.bestBid());
        assertEquals(102_00, snapshot.bestAsk());
        assertEquals(1, snapshot.asks().get(0).totalQuantity());
    }

    @Test
    void partiallyFilledGtcLimitOrderRestsTheRemainder() {
        OrderBook book = new OrderBook("AAPL");
        book.place(OrderRequest.limit("S-1", "AAPL", Side.SELL, 100_00, 7));

        ExecutionReport report = book.place(OrderRequest.limit("B-1", "AAPL", Side.BUY, 100_00, 10));

        assertEquals(OrderStatus.PARTIALLY_FILLED, report.status());
        assertEquals(3, report.remainingQuantity());
        assertEquals(7, report.trades().get(0).quantity());

        OrderBookSnapshot snapshot = book.snapshot(5);
        assertEquals(100_00, snapshot.bestBid());
        assertNull(snapshot.bestAsk());
        assertEquals(3, snapshot.bids().get(0).totalQuantity());
    }

    @Test
    void iocLimitOrderDoesNotRestUnfilledQuantity() {
        OrderBook book = new OrderBook("AAPL");
        book.place(OrderRequest.limit("S-1", "AAPL", Side.SELL, 101_00, 5));

        ExecutionReport report = book.place(
                OrderRequest.limit("B-1", "AAPL", Side.BUY, 100_00, 10, TimeInForce.IOC)
        );

        assertEquals(OrderStatus.CANCELLED, report.status());
        assertEquals(10, report.remainingQuantity());
        assertTrue(report.trades().isEmpty());

        OrderBookSnapshot snapshot = book.snapshot(5);
        assertNull(snapshot.bestBid());
        assertEquals(101_00, snapshot.bestAsk());
    }

    @Test
    void cancelRemovesActiveOrderFromBook() {
        OrderBook book = new OrderBook("AAPL");
        book.place(OrderRequest.limit("B-1", "AAPL", Side.BUY, 100_00, 10));

        ExecutionReport report = book.cancel("B-1");

        assertEquals(OrderStatus.CANCELLED, report.status());
        assertEquals(10, report.remainingQuantity());
        OrderBookSnapshot snapshot = book.snapshot(5);
        assertNull(snapshot.bestBid());
        assertTrue(snapshot.bids().isEmpty());
    }

    @Test
    void unknownCancelIsRejected() {
        OrderBook book = new OrderBook("AAPL");

        ExecutionReport report = book.cancel("missing");

        assertEquals(OrderStatus.REJECTED, report.status());
        assertTrue(report.rejected());
    }

    @Test
    void orderIdCannotBeReusedAfterFill() {
        OrderBook book = new OrderBook("AAPL");
        book.place(OrderRequest.limit("S-1", "AAPL", Side.SELL, 100_00, 1));
        book.place(OrderRequest.limit("B-1", "AAPL", Side.BUY, 100_00, 1));

        ExecutionReport duplicate = book.place(OrderRequest.limit("B-1", "AAPL", Side.BUY, 99_00, 1));

        assertEquals(OrderStatus.REJECTED, duplicate.status());
        assertEquals("Duplicate order id", duplicate.message());
    }
}
