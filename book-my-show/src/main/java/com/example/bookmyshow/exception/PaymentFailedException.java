package com.example.bookmyshow.exception;

public class PaymentFailedException extends RuntimeException {

    public PaymentFailedException(String bookingId) {
        super("payment failed for booking: " + bookingId);
    }
}
