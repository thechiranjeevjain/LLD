package com.example.bookmyshow.domain;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

public final class ShowInventory {
    private final Show show;
    private final Map<String, SeatState> seatsById = new LinkedHashMap<>();
    private final ReentrantLock lock = new ReentrantLock();

    public ShowInventory(Show show) {
        this.show = show;
        for (Seat seat : show.screen().seats()) {
            seatsById.put(seat.id(), new SeatState(seat));
        }
    }

    public Show show() {
        return show;
    }

    public void lock() {
        lock.lock();
    }

    public void unlock() {
        lock.unlock();
    }

    public Set<String> releaseExpiredHolds(Instant now) {
        Set<String> expiredBookingIds = new LinkedHashSet<>();
        for (SeatState state : seatsById.values()) {
            if (state.status == SeatStatus.HELD && !state.holdExpiresAt.isAfter(now)) {
                expiredBookingIds.add(state.bookingId);
                state.release();
            }
        }
        return expiredBookingIds;
    }

    public List<Seat> availableSeats(Instant now) {
        releaseExpiredHolds(now);
        List<Seat> seats = new ArrayList<>();
        for (SeatState state : seatsById.values()) {
            if (state.status == SeatStatus.AVAILABLE) {
                seats.add(state.seat);
            }
        }
        return List.copyOf(seats);
    }

    public List<String> unavailableSeatIds(Collection<String> requestedSeatIds) {
        List<String> unavailable = new ArrayList<>();
        for (String seatId : requestedSeatIds) {
            SeatState state = seatsById.get(seatId);
            if (state == null || state.status != SeatStatus.AVAILABLE) {
                unavailable.add(seatId);
            }
        }
        return unavailable;
    }

    public Money amountFor(Collection<String> seatIds) {
        Money total = null;
        for (String seatId : seatIds) {
            SeatState state = requireSeat(seatId);
            Money price = show.priceFor(state.seat);
            total = total == null ? price : total.add(price);
        }
        if (total == null) {
            throw new IllegalArgumentException("seatIds must not be empty");
        }
        return total;
    }

    public void holdSeats(String bookingId, Collection<String> seatIds, Instant holdExpiresAt) {
        for (String seatId : seatIds) {
            SeatState state = requireSeat(seatId);
            state.status = SeatStatus.HELD;
            state.bookingId = bookingId;
            state.holdExpiresAt = holdExpiresAt;
        }
    }

    public boolean ownsHold(String bookingId, Collection<String> seatIds) {
        for (String seatId : seatIds) {
            SeatState state = requireSeat(seatId);
            if (state.status != SeatStatus.HELD || !bookingId.equals(state.bookingId)) {
                return false;
            }
        }
        return true;
    }

    public void bookSeats(String bookingId, Collection<String> seatIds) {
        for (String seatId : seatIds) {
            SeatState state = requireSeat(seatId);
            if (state.status != SeatStatus.HELD || !bookingId.equals(state.bookingId)) {
                throw new IllegalStateException("seat is not held by booking " + bookingId + ": " + seatId);
            }
            state.status = SeatStatus.BOOKED;
            state.bookingId = bookingId;
            state.holdExpiresAt = null;
        }
    }

    public void releaseSeatsForBooking(String bookingId, Collection<String> seatIds) {
        for (String seatId : seatIds) {
            SeatState state = requireSeat(seatId);
            if (bookingId.equals(state.bookingId)) {
                state.release();
            }
        }
    }

    private SeatState requireSeat(String seatId) {
        SeatState state = seatsById.get(seatId);
        if (state == null) {
            throw new IllegalArgumentException("unknown seat id: " + seatId);
        }
        return state;
    }

    private static final class SeatState {
        private final Seat seat;
        private SeatStatus status = SeatStatus.AVAILABLE;
        private String bookingId;
        private Instant holdExpiresAt;

        private SeatState(Seat seat) {
            this.seat = seat;
        }

        private void release() {
            status = SeatStatus.AVAILABLE;
            bookingId = null;
            holdExpiresAt = null;
        }
    }
}
