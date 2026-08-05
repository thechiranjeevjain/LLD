package org.chijai.orderbook.engine;

import org.chijai.orderbook.model.ExecutionReport;
import org.chijai.orderbook.model.OrderBookSnapshot;
import org.chijai.orderbook.model.OrderRequest;

import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public final class MatchingEngine {
    private final Map<String, OrderBook> books = new ConcurrentHashMap<>();

    public ExecutionReport place(OrderRequest request) {
        Objects.requireNonNull(request, "request is required");
        return bookFor(request.symbol()).place(request);
    }

    public ExecutionReport cancel(String symbol, String orderId) {
        return bookFor(symbol).cancel(orderId);
    }

    public OrderBookSnapshot snapshot(String symbol, int depth) {
        return bookFor(symbol).snapshot(depth);
    }

    public OrderBook bookFor(String symbol) {
        String normalizedSymbol = normalizeSymbol(symbol);
        return books.computeIfAbsent(normalizedSymbol, OrderBook::new);
    }

    private String normalizeSymbol(String symbol) {
        Objects.requireNonNull(symbol, "symbol is required");
        String normalized = symbol.trim();
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("symbol must not be blank");
        }
        return normalized.toUpperCase(Locale.ROOT);
    }
}
