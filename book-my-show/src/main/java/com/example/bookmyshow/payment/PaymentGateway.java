package com.example.bookmyshow.payment;

import com.example.bookmyshow.domain.Booking;
import com.example.bookmyshow.domain.Payment;

public interface PaymentGateway {
    Payment charge(Booking booking);
}
