package com.chiranjeev.lld.matching;

import java.util.*;

/** One symbol, one serialized mutation stream: deterministic price-time priority. */
public final class MatchingEngine {
    private final NavigableMap<Long, Deque<Order>> bids = new TreeMap<>(Comparator.reverseOrder());
    private final NavigableMap<Long, Deque<Order>> asks = new TreeMap<>();
    private final Set<String> knownOrderIds = new HashSet<>();
    private long nextTradeSequence = 1;

    public synchronized MatchResult submit(Order incoming) {
        Objects.requireNonNull(incoming);
        if (!knownOrderIds.add(incoming.id())) throw new IllegalArgumentException("duplicate order id");
        List<Trade> trades = new ArrayList<>();
        NavigableMap<Long, Deque<Order>> opposite = incoming.side() == Side.BUY ? asks : bids;
        while (incoming.remainingQuantity() > 0 && !opposite.isEmpty()) {
            Map.Entry<Long, Deque<Order>> level = opposite.firstEntry();
            if (!crosses(incoming, level.getKey())) break;
            Deque<Order> fifo = level.getValue();
            while (incoming.remainingQuantity() > 0 && !fifo.isEmpty()) {
                Order maker = fifo.peekFirst();
                long fill = Math.min(incoming.remainingQuantity(), maker.remainingQuantity());
                incoming.fill(fill); maker.fill(fill);
                trades.add(new Trade(nextTradeSequence++, maker.id(), incoming.id(), maker.price(), fill));
                if (maker.remainingQuantity() == 0) fifo.removeFirst();
            }
            if (fifo.isEmpty()) opposite.remove(level.getKey());
        }
        boolean resting = incoming.type() == OrderType.LIMIT && incoming.remainingQuantity() > 0;
        if (resting) ownBook(incoming.side()).computeIfAbsent(incoming.price(), ignored -> new ArrayDeque<>()).addLast(incoming);
        return new MatchResult(incoming.id(), incoming.originalQuantity() - incoming.remainingQuantity(), incoming.remainingQuantity(), resting, trades);
    }

    public synchronized Long bestBid() { return bids.isEmpty() ? null : bids.firstKey(); }
    public synchronized Long bestAsk() { return asks.isEmpty() ? null : asks.firstKey(); }

    private boolean crosses(Order order, long makerPrice) {
        return order.type() == OrderType.MARKET || (order.side() == Side.BUY ? order.price() >= makerPrice : order.price() <= makerPrice);
    }
    private NavigableMap<Long, Deque<Order>> ownBook(Side side) { return side == Side.BUY ? bids : asks; }
}
