package com.chiranjeev.lld.risk;

public record RiskLimits(long maxQuantity, long maxNotional, long maxAbsoluteExposure, int maxPriceDeviationBps) {
    public RiskLimits { if (maxQuantity <= 0 || maxNotional <= 0 || maxAbsoluteExposure <= 0 || maxPriceDeviationBps < 0) throw new IllegalArgumentException("invalid limits"); }
}
