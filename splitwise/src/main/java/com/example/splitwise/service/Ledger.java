package com.example.splitwise.service;

import com.example.splitwise.domain.Debt;
import com.example.splitwise.domain.Money;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.PriorityQueue;

public final class Ledger {
    private final Map<DebtKey, Money> debts = new HashMap<>();

    public void addDebt(String debtorUserId, String creditorUserId, Money amount) {
        if (debtorUserId.equals(creditorUserId) || amount.isZero()) {
            return;
        }
        if (!amount.isPositive()) {
            throw new IllegalArgumentException("debt amount must be positive");
        }

        DebtKey direct = new DebtKey(debtorUserId, creditorUserId);
        DebtKey reverse = new DebtKey(creditorUserId, debtorUserId);
        Money reverseAmount = debts.get(reverse);

        if (reverseAmount == null) {
            debts.merge(direct, amount, Money::plus);
            return;
        }

        int comparison = reverseAmount.compareTo(amount);
        if (comparison > 0) {
            debts.put(reverse, reverseAmount.minus(amount));
        } else if (comparison < 0) {
            debts.remove(reverse);
            debts.put(direct, amount.minus(reverseAmount));
        } else {
            debts.remove(reverse);
        }
    }

    public void recordPayment(String payerUserId, String payeeUserId, Money amount) {
        if (!amount.isPositive()) {
            throw new IllegalArgumentException("payment amount must be positive");
        }
        addDebt(payeeUserId, payerUserId, amount);
    }

    public List<Debt> currentDebts() {
        return debts.entrySet().stream()
                .map(entry -> new Debt(entry.getKey().fromUserId(), entry.getKey().toUserId(), entry.getValue()))
                .sorted(debtComparator())
                .toList();
    }

    public List<Debt> simplifiedDebts() {
        Map<String, Map<String, Money>> netByCurrency = new HashMap<>();
        for (Map.Entry<DebtKey, Money> entry : debts.entrySet()) {
            DebtKey key = entry.getKey();
            Money amount = entry.getValue();
            Map<String, Money> netByUser = netByCurrency.computeIfAbsent(amount.currency(), ignored -> new HashMap<>());
            netByUser.merge(key.fromUserId(), amount.negate(), Money::plus);
            netByUser.merge(key.toUserId(), amount, Money::plus);
        }

        List<Debt> simplified = new ArrayList<>();
        for (Map.Entry<String, Map<String, Money>> entry : netByCurrency.entrySet()) {
            simplified.addAll(simplifyCurrency(entry.getKey(), entry.getValue()));
        }
        simplified.sort(debtComparator());
        return simplified;
    }

    private List<Debt> simplifyCurrency(String currency, Map<String, Money> netByUser) {
        PriorityQueue<AccountBalance> debtors = new PriorityQueue<>(AccountBalance.largestAmountFirst());
        PriorityQueue<AccountBalance> creditors = new PriorityQueue<>(AccountBalance.largestAmountFirst());

        for (Map.Entry<String, Money> entry : netByUser.entrySet()) {
            Money balance = entry.getValue();
            if (balance.isNegative()) {
                debtors.add(new AccountBalance(entry.getKey(), balance.abs()));
            } else if (balance.isPositive()) {
                creditors.add(new AccountBalance(entry.getKey(), balance));
            }
        }

        List<Debt> result = new ArrayList<>();
        while (!debtors.isEmpty() && !creditors.isEmpty()) {
            AccountBalance debtor = debtors.poll();
            AccountBalance creditor = creditors.poll();
            Money amount = debtor.amount().min(creditor.amount());

            result.add(new Debt(debtor.userId(), creditor.userId(), amount));

            Money debtorRemaining = debtor.amount().minus(amount);
            Money creditorRemaining = creditor.amount().minus(amount);
            if (debtorRemaining.isPositive()) {
                debtors.add(new AccountBalance(debtor.userId(), debtorRemaining));
            }
            if (creditorRemaining.isPositive()) {
                creditors.add(new AccountBalance(creditor.userId(), creditorRemaining));
            }
        }

        if (!debtors.isEmpty() || !creditors.isEmpty()) {
            throw new IllegalStateException("ledger is unbalanced for " + currency);
        }
        return result;
    }

    private static Comparator<Debt> debtComparator() {
        return Comparator.comparing(Debt::fromUserId)
                .thenComparing(Debt::toUserId)
                .thenComparing(debt -> debt.amount().currency());
    }

    private record DebtKey(String fromUserId, String toUserId) {
        private DebtKey {
            if (Objects.requireNonNull(fromUserId, "fromUserId").isBlank()) {
                throw new IllegalArgumentException("fromUserId is required");
            }
            if (Objects.requireNonNull(toUserId, "toUserId").isBlank()) {
                throw new IllegalArgumentException("toUserId is required");
            }
        }
    }

    private record AccountBalance(String userId, Money amount) {
        private static Comparator<AccountBalance> largestAmountFirst() {
            return Comparator.<AccountBalance, Money>comparing(AccountBalance::amount).reversed()
                    .thenComparing(AccountBalance::userId);
        }
    }
}
