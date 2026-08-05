package com.example.splitwise.service;

import com.example.splitwise.domain.Debt;
import com.example.splitwise.domain.Expense;
import com.example.splitwise.domain.ExpenseShare;
import com.example.splitwise.domain.Group;
import com.example.splitwise.domain.Money;
import com.example.splitwise.domain.SplitInput;
import com.example.splitwise.domain.SplitType;
import com.example.splitwise.domain.User;

import java.time.Clock;
import java.time.Instant;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public final class SplitwiseService {
    private final Map<String, User> users = new LinkedHashMap<>();
    private final Map<String, Group> groups = new LinkedHashMap<>();
    private final Map<String, Expense> expenses = new LinkedHashMap<>();
    private final Map<SplitType, SplitStrategy> splitStrategies = new EnumMap<>(SplitType.class);
    private final Ledger ledger = new Ledger();
    private final Clock clock;

    public SplitwiseService() {
        this(Clock.systemUTC());
    }

    public SplitwiseService(Clock clock) {
        this.clock = Objects.requireNonNull(clock, "clock");
        splitStrategies.put(SplitType.EQUAL, new EqualSplitStrategy());
        splitStrategies.put(SplitType.EXACT, new ExactSplitStrategy());
        splitStrategies.put(SplitType.PERCENTAGE, new PercentageSplitStrategy());
    }

    public User createUser(String name, String email) {
        User user = new User(newId(), name, email);
        users.put(user.id(), user);
        return user;
    }

    public Group createGroup(String name, List<String> memberIds) {
        Objects.requireNonNull(memberIds, "memberIds").forEach(this::requireUser);
        Group group = new Group(newId(), name, memberIds);
        groups.put(group.id(), group);
        return group;
    }

    public void addMember(String groupId, String userId) {
        requireUser(userId);
        requireGroup(groupId).addMember(userId);
    }

    public Expense addExpense(
            String groupId,
            String paidByUserId,
            Money amount,
            String description,
            SplitType splitType,
            List<SplitInput> splitInputs
    ) {
        Group group = requireGroup(groupId);
        requireGroupMember(group, paidByUserId);
        Objects.requireNonNull(amount, "amount");
        Objects.requireNonNull(splitType, "splitType");
        List<SplitInput> safeSplitInputs = List.copyOf(Objects.requireNonNull(splitInputs, "splitInputs"));
        safeSplitInputs.forEach(input -> requireGroupMember(group, input.userId()));

        Map<String, Money> calculatedShares = splitStrategies.get(splitType).split(amount, safeSplitInputs);
        List<ExpenseShare> shares = calculatedShares.entrySet().stream()
                .map(entry -> new ExpenseShare(entry.getKey(), entry.getValue()))
                .toList();

        Expense expense = new Expense(
                newId(),
                groupId,
                paidByUserId,
                amount,
                description,
                splitType,
                shares,
                Instant.now(clock)
        );

        for (ExpenseShare share : shares) {
            ledger.addDebt(share.userId(), paidByUserId, share.amount());
        }
        expenses.put(expense.id(), expense);
        return expense;
    }

    public void recordPayment(String payerUserId, String payeeUserId, Money amount) {
        requireUser(payerUserId);
        requireUser(payeeUserId);
        ledger.recordPayment(payerUserId, payeeUserId, amount);
    }

    public List<Debt> balances() {
        return ledger.currentDebts();
    }

    public List<Debt> simplifiedBalances() {
        return ledger.simplifiedDebts();
    }

    public List<Expense> expenses() {
        return List.copyOf(expenses.values());
    }

    private User requireUser(String userId) {
        User user = users.get(userId);
        if (user == null) {
            throw new IllegalArgumentException("unknown user: " + userId);
        }
        return user;
    }

    private Group requireGroup(String groupId) {
        Group group = groups.get(groupId);
        if (group == null) {
            throw new IllegalArgumentException("unknown group: " + groupId);
        }
        return group;
    }

    private void requireGroupMember(Group group, String userId) {
        requireUser(userId);
        if (!group.hasMember(userId)) {
            throw new IllegalArgumentException("user " + userId + " is not a member of group " + group.id());
        }
    }

    private static String newId() {
        return UUID.randomUUID().toString();
    }
}
