package com.example.splitwise.service;

import com.example.splitwise.domain.Debt;
import com.example.splitwise.domain.Group;
import com.example.splitwise.domain.Money;
import com.example.splitwise.domain.SplitInput;
import com.example.splitwise.domain.SplitType;
import com.example.splitwise.domain.User;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SplitwiseServiceTest {

    private final SplitwiseService splitwise = new SplitwiseService(
            Clock.fixed(Instant.parse("2026-08-05T00:00:00Z"), ZoneOffset.UTC)
    );

    @Test
    void equalExpenseCreatesDebtsFromParticipantsToPayer() {
        Fixture fixture = fixture();

        splitwise.addExpense(
                fixture.group().id(),
                fixture.alice().id(),
                Money.of("INR", "90.00"),
                "Dinner",
                SplitType.EQUAL,
                List.of(
                        SplitInput.forUser(fixture.alice().id()),
                        SplitInput.forUser(fixture.bob().id()),
                        SplitInput.forUser(fixture.cara().id())
                )
        );

        assertDebts(
                splitwise.balances(),
                debt(fixture.bob(), fixture.alice(), "30.00"),
                debt(fixture.cara(), fixture.alice(), "30.00")
        );
    }

    @Test
    void exactExpenseNetsOppositeDebts() {
        Fixture fixture = fixture();

        splitwise.addExpense(
                fixture.group().id(),
                fixture.alice().id(),
                Money.of("INR", "100.00"),
                "Hotel",
                SplitType.EXACT,
                List.of(
                        SplitInput.exact(fixture.alice().id(), Money.of("INR", "40.00")),
                        SplitInput.exact(fixture.bob().id(), Money.of("INR", "60.00"))
                )
        );
        splitwise.addExpense(
                fixture.group().id(),
                fixture.bob().id(),
                Money.of("INR", "30.00"),
                "Fuel",
                SplitType.EXACT,
                List.of(SplitInput.exact(fixture.alice().id(), Money.of("INR", "30.00")))
        );

        assertEquals(List.of(debt(fixture.bob(), fixture.alice(), "30.00")), splitwise.balances());
    }

    @Test
    void percentageExpenseAllocatesRoundingRemainderByLargestFraction() {
        Fixture fixture = fixture();

        splitwise.addExpense(
                fixture.group().id(),
                fixture.alice().id(),
                Money.of("INR", "10.00"),
                "Snacks",
                SplitType.PERCENTAGE,
                List.of(
                        SplitInput.percentage(fixture.alice().id(), new BigDecimal("33.33")),
                        SplitInput.percentage(fixture.bob().id(), new BigDecimal("33.33")),
                        SplitInput.percentage(fixture.cara().id(), new BigDecimal("33.34"))
                )
        );

        assertDebts(
                splitwise.balances(),
                debt(fixture.bob(), fixture.alice(), "3.33"),
                debt(fixture.cara(), fixture.alice(), "3.34")
        );
    }

    @Test
    void simplifiedBalancesMinimizeNumberOfSettlementPayments() {
        Fixture fixture = fixture();

        splitwise.addExpense(
                fixture.group().id(),
                fixture.alice().id(),
                Money.of("INR", "120.00"),
                "Dinner",
                SplitType.EQUAL,
                List.of(
                        SplitInput.forUser(fixture.alice().id()),
                        SplitInput.forUser(fixture.bob().id()),
                        SplitInput.forUser(fixture.cara().id())
                )
        );
        splitwise.addExpense(
                fixture.group().id(),
                fixture.bob().id(),
                Money.of("INR", "60.00"),
                "Taxi",
                SplitType.EQUAL,
                List.of(SplitInput.forUser(fixture.bob().id()), SplitInput.forUser(fixture.cara().id()))
        );

        assertDebts(
                splitwise.simplifiedBalances(),
                debt(fixture.bob(), fixture.alice(), "10.00"),
                debt(fixture.cara(), fixture.alice(), "70.00")
        );
    }

    @Test
    void paymentReducesExistingDebtAndCanFullySettleIt() {
        Fixture fixture = fixture();

        splitwise.addExpense(
                fixture.group().id(),
                fixture.alice().id(),
                Money.of("INR", "60.00"),
                "Lunch",
                SplitType.EQUAL,
                List.of(SplitInput.forUser(fixture.alice().id()), SplitInput.forUser(fixture.bob().id()))
        );

        splitwise.recordPayment(fixture.bob().id(), fixture.alice().id(), Money.of("INR", "10.00"));
        assertEquals(List.of(debt(fixture.bob(), fixture.alice(), "20.00")), splitwise.balances());

        splitwise.recordPayment(fixture.bob().id(), fixture.alice().id(), Money.of("INR", "20.00"));
        assertTrue(splitwise.balances().isEmpty());
    }

    @Test
    void rejectsExactSplitWhenSharesDoNotMatchTotal() {
        Fixture fixture = fixture();

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> splitwise.addExpense(
                        fixture.group().id(),
                        fixture.alice().id(),
                        Money.of("INR", "100.00"),
                        "Hotel",
                        SplitType.EXACT,
                        List.of(
                                SplitInput.exact(fixture.alice().id(), Money.of("INR", "40.00")),
                                SplitInput.exact(fixture.bob().id(), Money.of("INR", "50.00"))
                        )
                )
        );

        assertTrue(exception.getMessage().contains("does not match"));
    }

    @Test
    void rejectsExpenseForUserOutsideGroup() {
        User alice = splitwise.createUser("Alice", "alice@example.com");
        User bob = splitwise.createUser("Bob", "bob@example.com");
        Group group = splitwise.createGroup("Trip", List.of(alice.id()));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> splitwise.addExpense(
                        group.id(),
                        alice.id(),
                        Money.of("INR", "100.00"),
                        "Hotel",
                        SplitType.EQUAL,
                        List.of(SplitInput.forUser(alice.id()), SplitInput.forUser(bob.id()))
                )
        );

        assertTrue(exception.getMessage().contains("is not a member"));
    }

    private Fixture fixture() {
        User alice = splitwise.createUser("Alice", "alice@example.com");
        User bob = splitwise.createUser("Bob", "bob@example.com");
        User cara = splitwise.createUser("Cara", "cara@example.com");
        Group group = splitwise.createGroup("Trip", List.of(alice.id(), bob.id(), cara.id()));
        return new Fixture(alice, bob, cara, group);
    }

    private static Debt debt(User from, User to, String amount) {
        return new Debt(from.id(), to.id(), Money.of("INR", amount));
    }

    private static void assertDebts(List<Debt> actual, Debt... expected) {
        assertEquals(expected.length, actual.size());
        assertEquals(Set.of(expected), new HashSet<>(actual));
    }

    private record Fixture(User alice, User bob, User cara, Group group) {
    }
}
