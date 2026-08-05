package com.example.bookmyshow.repository;

import com.example.bookmyshow.domain.Booking;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class InMemoryBookingRepository {
    private final ConcurrentMap<String, Booking> bookingsById = new ConcurrentHashMap<>();

    public Booking save(Booking booking) {
        bookingsById.put(booking.id(), booking);
        return booking;
    }

    public Optional<Booking> findById(String bookingId) {
        return Optional.ofNullable(bookingsById.get(bookingId));
    }
}
