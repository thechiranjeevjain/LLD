package com.chiranjeev.lld.fixgateway.risk;

public record RiskResult(boolean accepted, String reason) {
    public static RiskResult pass() {
        return new RiskResult(true, "Accepted");
    }

    public static RiskResult fail(String reason) {
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("reason is required");
        }
        return new RiskResult(false, reason);
    }
}
