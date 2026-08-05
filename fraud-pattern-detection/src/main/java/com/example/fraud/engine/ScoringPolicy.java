package com.example.fraud.engine;

import com.example.fraud.domain.DecisionType;

public class ScoringPolicy {
    private final int reviewThreshold;
    private final int blockThreshold;

    public ScoringPolicy(int reviewThreshold, int blockThreshold) {
        if (reviewThreshold <= 0 || blockThreshold <= reviewThreshold) {
            throw new IllegalArgumentException("thresholds must satisfy 0 < review < block");
        }
        this.reviewThreshold = reviewThreshold;
        this.blockThreshold = blockThreshold;
    }

    public DecisionType decide(int riskScore) {
        if (riskScore >= blockThreshold) {
            return DecisionType.BLOCK;
        }
        if (riskScore >= reviewThreshold) {
            return DecisionType.REVIEW;
        }
        return DecisionType.APPROVE;
    }
}
