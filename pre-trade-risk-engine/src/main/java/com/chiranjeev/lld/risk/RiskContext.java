package com.chiranjeev.lld.risk;

public record RiskContext(long currentExposure, long referencePrice, RiskLimits limits, boolean killSwitchEnabled) { }
