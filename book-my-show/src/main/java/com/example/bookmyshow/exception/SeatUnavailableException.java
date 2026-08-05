package com.example.bookmyshow.exception;

import java.util.List;

public class SeatUnavailableException extends RuntimeException {
    private final String showId;
    private final List<String> seatIds;

    public SeatUnavailableException(String showId, List<String> seatIds) {
        super("seats are unavailable for show " + showId + ": " + seatIds);
        this.showId = showId;
        this.seatIds = List.copyOf(seatIds);
    }

    public String showId() {
        return showId;
    }

    public List<String> seatIds() {
        return seatIds;
    }
}
