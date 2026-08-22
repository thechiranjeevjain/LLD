package com.chiranjeev.lld.risk;

import java.util.Objects;

public record OrderRequest(String accountId, String symbol, Side side, long quantity, long limitPrice) {
    public OrderRequest {
        Objects.requireNonNull(accountId); Objects.requireNonNull(symbol); Objects.requireNonNull(side);
        if (accountId.isBlank() || symbol.isBlank()) throw new IllegalArgumentException("account and symbol are required");
        if (quantity <= 0 || limitPrice <= 0) throw new IllegalArgumentException("quantity and price must be positive");
    }
    public long notional() { return Math.multiplyExact(quantity, limitPrice); }
    public long signedQuantity() { return Math.multiplyExact(quantity, side.sign()); }
}
