package com.example.splitwise;

import com.example.splitwise.domain.Debt;
import com.example.splitwise.domain.Group;
import com.example.splitwise.domain.Money;
import com.example.splitwise.domain.SplitInput;
import com.example.splitwise.domain.SplitType;
import com.example.splitwise.domain.User;
import com.example.splitwise.service.SplitwiseService;

import java.math.BigDecimal;
import java.util.List;

public class SplitwiseApplication {

    public static void main(String[] args) {
        SplitwiseService splitwise = new SplitwiseService();

        User alice = splitwise.createUser("Alice", "alice@example.com");
        User bob = splitwise.createUser("Bob", "bob@example.com");
        User cara = splitwise.createUser("Cara", "cara@example.com");

        Group trip = splitwise.createGroup("Goa trip", List.of(alice.id(), bob.id(), cara.id()));

        splitwise.addExpense(
                trip.id(),
                alice.id(),
                Money.of("INR", "900.00"),
                "Dinner",
                SplitType.EQUAL,
                List.of(SplitInput.forUser(alice.id()), SplitInput.forUser(bob.id()), SplitInput.forUser(cara.id()))
        );

        splitwise.addExpense(
                trip.id(),
                bob.id(),
                Money.of("INR", "300.00"),
                "Taxi",
                SplitType.PERCENTAGE,
                List.of(
                        SplitInput.percentage(alice.id(), new BigDecimal("50")),
                        SplitInput.percentage(bob.id(), new BigDecimal("25")),
                        SplitInput.percentage(cara.id(), new BigDecimal("25"))
                )
        );

        for (Debt debt : splitwise.simplifiedBalances()) {
            System.out.println(debt);
        }
    }
}
