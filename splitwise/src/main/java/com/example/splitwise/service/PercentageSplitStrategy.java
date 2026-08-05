package com.example.splitwise.service;

import com.example.splitwise.domain.Money;
import com.example.splitwise.domain.SplitInput;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

final class PercentageSplitStrategy implements SplitStrategy {
    private static final BigDecimal ONE_HUNDRED = new BigDecimal("100.00");

    @Override
    public Map<String, Money> split(Money total, List<SplitInput> inputs) {
        if (inputs.isEmpty()) {
            throw new IllegalArgumentException("percentage split requires at least one participant");
        }

        BigDecimal percentageSum = BigDecimal.ZERO;
        List<CalculatedShare> calculatedShares = new ArrayList<>();
        long allocatedMinorUnits = 0;

        for (SplitInput input : inputs) {
            if (calculatedShares.stream().anyMatch(share -> share.userId().equals(input.userId()))) {
                throw new IllegalArgumentException("duplicate split participant: " + input.userId());
            }
            BigDecimal percentage = Objects.requireNonNull(input.percentage(), "percentage is required");
            if (percentage.signum() <= 0) {
                throw new IllegalArgumentException("percentage must be positive");
            }
            percentageSum = percentageSum.add(percentage);

            BigDecimal exactMinorUnits = BigDecimal.valueOf(total.toMinorUnits())
                    .multiply(percentage)
                    .divide(ONE_HUNDRED, 8, RoundingMode.HALF_UP);
            long floorMinorUnits = exactMinorUnits.setScale(0, RoundingMode.DOWN).longValueExact();
            BigDecimal remainder = exactMinorUnits.subtract(BigDecimal.valueOf(floorMinorUnits));
            calculatedShares.add(new CalculatedShare(input.userId(), floorMinorUnits, remainder));
            allocatedMinorUnits += floorMinorUnits;
        }

        if (percentageSum.compareTo(ONE_HUNDRED) != 0) {
            throw new IllegalArgumentException("percentage split must total 100, got " + percentageSum);
        }

        long remainingMinorUnits = total.toMinorUnits() - allocatedMinorUnits;
        calculatedShares.stream()
                .sorted(Comparator.comparing(CalculatedShare::remainder).reversed().thenComparing(CalculatedShare::userId))
                .limit(remainingMinorUnits)
                .forEach(CalculatedShare::addOneMinorUnit);

        Map<String, Money> shares = new LinkedHashMap<>();
        for (CalculatedShare share : calculatedShares) {
            shares.put(share.userId(), Money.fromMinorUnits(total.currency(), share.minorUnits()));
        }
        return shares;
    }

    private static final class CalculatedShare {
        private final String userId;
        private long minorUnits;
        private final BigDecimal remainder;

        private CalculatedShare(String userId, long minorUnits, BigDecimal remainder) {
            this.userId = userId;
            this.minorUnits = minorUnits;
            this.remainder = remainder;
        }

        private String userId() {
            return userId;
        }

        private long minorUnits() {
            return minorUnits;
        }

        private BigDecimal remainder() {
            return remainder;
        }

        private void addOneMinorUnit() {
            minorUnits++;
        }
    }
}
