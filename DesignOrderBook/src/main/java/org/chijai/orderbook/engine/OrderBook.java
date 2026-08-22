package org.chijai.orderbook.engine;

import org.chijai.orderbook.model.ExecutionReport;
import org.chijai.orderbook.model.Order;
import org.chijai.orderbook.model.OrderBookSnapshot;
import org.chijai.orderbook.model.OrderRequest;
import org.chijai.orderbook.model.OrderStatus;
import org.chijai.orderbook.model.OrderType;
import org.chijai.orderbook.model.PriceLevel;
import org.chijai.orderbook.model.Side;
import org.chijai.orderbook.model.TimeInForce;
import org.chijai.orderbook.model.Trade;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;

public final class OrderBook {
    private final String symbol;
    private final NavigableMap<Long, Deque<Order>> bids = new TreeMap<>(Comparator.reverseOrder());
    private final NavigableMap<Long, Deque<Order>> asks = new TreeMap<>();
    private final Map<String, Order> activeOrders = new HashMap<>();
    private final Set<String> knownOrderIds = new HashSet<>();
    private long nextOrderSequence = 1;
    private long nextTradeSequence = 1;

    public OrderBook(String symbol) {
        Objects.requireNonNull(symbol, "symbol is required");
        String normalizedSymbol = symbol.trim();
        if (normalizedSymbol.isEmpty()) {
            throw new IllegalArgumentException("symbol must not be blank");
        }
        this.symbol = normalizedSymbol.toUpperCase(Locale.ROOT);
    }

    public synchronized ExecutionReport place(OrderRequest request) {
        Objects.requireNonNull(request, "request is required");
        if (!symbol.equals(request.symbol())) {
            return ExecutionReport.rejected(request, "Order belongs to " + request.symbol() + ", not " + symbol);
        }
        if (knownOrderIds.contains(request.orderId())) {
            return ExecutionReport.rejected(request, "Duplicate order id");
        }

        knownOrderIds.add(request.orderId());
        Order incoming = Order.from(request, nextOrderSequence++);
        List<Trade> trades = match(incoming);

        if (incoming.remainingQuantity() == 0) {
            return ExecutionReport.from(incoming, trades, "Fully filled");
        }

        if (shouldRest(incoming, request.timeInForce())) {
            rest(incoming);
            String message = trades.isEmpty() ? "Accepted and resting" : "Partially filled and resting";
            return ExecutionReport.from(incoming, trades, message);
        }

        incoming.cancel();
        String message = trades.isEmpty() ? "No matching liquidity" : "Unfilled quantity expired";
        return ExecutionReport.from(incoming, trades, message);
    }

    public synchronized ExecutionReport cancel(String orderId) {
        Objects.requireNonNull(orderId, "orderId is required");
        Order order = activeOrders.remove(orderId);
        if (order == null) {
            return ExecutionReport.rejected(orderId, symbol, "Order is not active");
        }

        removeRestingOrder(order);
        order.cancel();
        return ExecutionReport.from(order, List.of(), "Cancelled");
    }

    /**
     * Cancel-replace semantics: the replacement receives a new order id and loses time priority.
     * The requested quantity is the replacement's new open quantity, not the old order's total quantity.
     */
    public synchronized ExecutionReport replace(String orderId, String replacementOrderId, long newPrice, long newQuantity) {
        Objects.requireNonNull(orderId, "orderId is required");
        Objects.requireNonNull(replacementOrderId, "replacementOrderId is required");
        Order current = activeOrders.get(orderId);
        if (current == null) {
            return ExecutionReport.rejected(replacementOrderId, symbol, "Order is not active");
        }
        if (knownOrderIds.contains(replacementOrderId)) {
            return ExecutionReport.rejected(replacementOrderId, symbol, "Duplicate replacement order id");
        }
        if (newPrice <= 0 || newQuantity <= 0) {
            return ExecutionReport.rejected(replacementOrderId, symbol, "Replacement price and quantity must be positive");
        }

        activeOrders.remove(orderId);
        removeRestingOrder(current);
        current.cancel();
        return place(OrderRequest.limit(
                replacementOrderId,
                symbol,
                current.side(),
                newPrice,
                newQuantity,
                TimeInForce.GTC
        ));
    }

    public synchronized OrderBookSnapshot snapshot() {
        return snapshot(Integer.MAX_VALUE);
    }

    public synchronized OrderBookSnapshot snapshot(int depth) {
        if (depth <= 0) {
            throw new IllegalArgumentException("depth must be positive");
        }
        return new OrderBookSnapshot(
                symbol,
                bids.isEmpty() ? null : bids.firstKey(),
                asks.isEmpty() ? null : asks.firstKey(),
                summarize(bids, depth),
                summarize(asks, depth)
        );
    }

    public String symbol() {
        return symbol;
    }

    private List<Trade> match(Order incoming) {
        List<Trade> trades = new ArrayList<>();
        NavigableMap<Long, Deque<Order>> oppositeBook = incoming.side() == Side.BUY ? asks : bids;

        while (incoming.remainingQuantity() > 0 && !oppositeBook.isEmpty()) {
            Map.Entry<Long, Deque<Order>> bestLevel = oppositeBook.firstEntry();
            long restingPrice = bestLevel.getKey();
            if (!canMatch(incoming, restingPrice)) {
                break;
            }

            Deque<Order> queue = bestLevel.getValue();
            while (incoming.remainingQuantity() > 0 && !queue.isEmpty()) {
                Order resting = queue.peekFirst();
                long quantity = Math.min(incoming.remainingQuantity(), resting.remainingQuantity());

                incoming.fill(quantity);
                resting.fill(quantity);
                trades.add(new Trade(
                        symbol,
                        incoming.orderId(),
                        resting.orderId(),
                        incoming.side(),
                        resting.price(),
                        quantity,
                        nextTradeSequence++
                ));

                if (!resting.isOpen()) {
                    queue.removeFirst();
                    activeOrders.remove(resting.orderId());
                }
            }

            if (queue.isEmpty()) {
                oppositeBook.remove(restingPrice);
            }
        }

        return trades;
    }

    private boolean canMatch(Order incoming, long restingPrice) {
        if (incoming.type() == OrderType.MARKET) {
            return true;
        }
        if (incoming.side() == Side.BUY) {
            return incoming.price() >= restingPrice;
        }
        return incoming.price() <= restingPrice;
    }

    private boolean shouldRest(Order order, TimeInForce timeInForce) {
        return order.type() == OrderType.LIMIT
                && timeInForce == TimeInForce.GTC
                && order.status() != OrderStatus.CANCELLED
                && order.remainingQuantity() > 0;
    }

    private void rest(Order order) {
        NavigableMap<Long, Deque<Order>> ownBook = order.side() == Side.BUY ? bids : asks;
        ownBook.computeIfAbsent(order.price(), ignored -> new ArrayDeque<>()).addLast(order);
        activeOrders.put(order.orderId(), order);
    }

    private void removeRestingOrder(Order order) {
        NavigableMap<Long, Deque<Order>> ownBook = order.side() == Side.BUY ? bids : asks;
        Deque<Order> level = ownBook.get(order.price());
        if (level == null) {
            return;
        }

        level.removeIf(resting -> resting.orderId().equals(order.orderId()));
        if (level.isEmpty()) {
            ownBook.remove(order.price());
        }
    }

    private List<PriceLevel> summarize(NavigableMap<Long, Deque<Order>> levels, int depth) {
        List<PriceLevel> result = new ArrayList<>();
        for (Map.Entry<Long, Deque<Order>> entry : levels.entrySet()) {
            if (result.size() == depth) {
                break;
            }

            long totalQuantity = entry.getValue().stream()
                    .mapToLong(Order::remainingQuantity)
                    .sum();
            result.add(new PriceLevel(entry.getKey(), totalQuantity, entry.getValue().size()));
        }
        return result;
    }
}
