package com.example.bookmyshow.payment;

import com.example.bookmyshow.domain.Booking;
import com.example.bookmyshow.domain.Payment;
import com.example.bookmyshow.domain.PaymentStatus;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicLong;

public class AlwaysSuccessfulPaymentGateway implements PaymentGateway {
    private final AtomicLong paymentSequence = new AtomicLong();

    @Override
    public Payment charge(Booking booking) {
        String paymentId = "payment-" + paymentSequence.incrementAndGet();
        return new Payment(
                paymentId,
                booking.id(),
                booking.amount(),
                PaymentStatus.SUCCESS,
                Instant.now(),
                "mock-ref-" + paymentId
        );
    }
}
