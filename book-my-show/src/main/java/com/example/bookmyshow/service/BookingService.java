package com.example.bookmyshow.service;

import com.example.bookmyshow.domain.Booking;
import com.example.bookmyshow.domain.BookingStatus;
import com.example.bookmyshow.domain.Payment;
import com.example.bookmyshow.domain.PaymentStatus;
import com.example.bookmyshow.domain.Seat;
import com.example.bookmyshow.domain.ShowInventory;
import com.example.bookmyshow.exception.BookingNotFoundException;
import com.example.bookmyshow.exception.PaymentFailedException;
import com.example.bookmyshow.exception.SeatUnavailableException;
import com.example.bookmyshow.payment.PaymentGateway;
import com.example.bookmyshow.repository.InMemoryBookingRepository;
import com.example.bookmyshow.repository.InMemoryShowRepository;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

public class BookingService {
    private final InMemoryShowRepository showRepository;
    private final InMemoryBookingRepository bookingRepository;
    private final PaymentGateway paymentGateway;
    private final Clock clock;
    private final Duration holdDuration;
    private final AtomicLong bookingSequence = new AtomicLong();

    public BookingService(
            InMemoryShowRepository showRepository,
            InMemoryBookingRepository bookingRepository,
            PaymentGateway paymentGateway,
            Clock clock,
            Duration holdDuration
    ) {
        this.showRepository = Objects.requireNonNull(showRepository, "showRepository");
        this.bookingRepository = Objects.requireNonNull(bookingRepository, "bookingRepository");
        this.paymentGateway = Objects.requireNonNull(paymentGateway, "paymentGateway");
        this.clock = Objects.requireNonNull(clock, "clock");
        this.holdDuration = Objects.requireNonNull(holdDuration, "holdDuration");
        if (holdDuration.isZero() || holdDuration.isNegative()) {
            throw new IllegalArgumentException("holdDuration must be positive");
        }
    }

    public Booking holdSeats(String userId, String showId, List<String> seatIds) {
        requireText(userId, "userId");
        requireText(showId, "showId");
        List<String> requestedSeatIds = normalizedSeatIds(seatIds);
        Instant now = clock.instant();
        Instant holdExpiresAt = now.plus(holdDuration);

        ShowInventory inventory = showRepository.inventoryFor(showId);
        inventory.lock();
        try {
            expireHolds(inventory.releaseExpiredHolds(now));

            List<String> unavailableSeatIds = inventory.unavailableSeatIds(requestedSeatIds);
            if (!unavailableSeatIds.isEmpty()) {
                throw new SeatUnavailableException(showId, unavailableSeatIds);
            }

            String bookingId = "booking-" + bookingSequence.incrementAndGet();
            Booking booking = new Booking(
                    bookingId,
                    userId,
                    showId,
                    requestedSeatIds,
                    inventory.amountFor(requestedSeatIds),
                    now,
                    holdExpiresAt
            );
            inventory.holdSeats(bookingId, requestedSeatIds, holdExpiresAt);
            return bookingRepository.save(booking);
        } finally {
            inventory.unlock();
        }
    }

    public Booking confirmBooking(String bookingId) {
        Booking booking = findBooking(bookingId);
        ShowInventory inventory = showRepository.inventoryFor(booking.showId());
        Instant now = clock.instant();

        inventory.lock();
        try {
            expireHolds(inventory.releaseExpiredHolds(now));
            if (booking.status() != BookingStatus.PENDING || booking.isHoldExpired(now)) {
                booking.expire();
                throw new SeatUnavailableException(booking.showId(), booking.seatIds());
            }
            if (!inventory.ownsHold(booking.id(), booking.seatIds())) {
                throw new SeatUnavailableException(booking.showId(), booking.seatIds());
            }

            Payment payment = paymentGateway.charge(booking);
            if (payment.status() != PaymentStatus.SUCCESS) {
                inventory.releaseSeatsForBooking(booking.id(), booking.seatIds());
                booking.cancel();
                throw new PaymentFailedException(booking.id());
            }

            inventory.bookSeats(booking.id(), booking.seatIds());
            booking.confirm(payment.id(), now);
            return booking;
        } finally {
            inventory.unlock();
        }
    }

    public void cancelBooking(String bookingId) {
        Booking booking = findBooking(bookingId);
        ShowInventory inventory = showRepository.inventoryFor(booking.showId());
        inventory.lock();
        try {
            if (booking.status() == BookingStatus.PENDING || booking.status() == BookingStatus.CONFIRMED) {
                inventory.releaseSeatsForBooking(booking.id(), booking.seatIds());
            }
            booking.cancel();
        } finally {
            inventory.unlock();
        }
    }

    public List<Seat> availableSeats(String showId) {
        ShowInventory inventory = showRepository.inventoryFor(showId);
        Instant now = clock.instant();
        inventory.lock();
        try {
            expireHolds(inventory.releaseExpiredHolds(now));
            return inventory.availableSeats(now);
        } finally {
            inventory.unlock();
        }
    }

    private Booking findBooking(String bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException(bookingId));
    }

    private void expireHolds(Set<String> expiredBookingIds) {
        for (String expiredBookingId : expiredBookingIds) {
            bookingRepository.findById(expiredBookingId).ifPresent(Booking::expire);
        }
    }

    private static List<String> normalizedSeatIds(List<String> seatIds) {
        if (seatIds == null || seatIds.isEmpty()) {
            throw new IllegalArgumentException("seatIds must not be empty");
        }
        Set<String> uniqueSeatIds = new LinkedHashSet<>();
        for (String seatId : seatIds) {
            uniqueSeatIds.add(requireText(seatId, "seatId"));
        }
        if (uniqueSeatIds.size() != seatIds.size()) {
            throw new IllegalArgumentException("seatIds must not contain duplicates");
        }
        return List.copyOf(uniqueSeatIds);
    }

    private static String requireText(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value;
    }
}
