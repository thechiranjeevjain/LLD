package com.chiranjeev.lld.matching;

import java.util.List;

public record MatchResult(String orderId, long filledQuantity, long remainingQuantity, boolean resting, List<Trade> trades) {
    public MatchResult { trades = List.copyOf(trades); }
}
