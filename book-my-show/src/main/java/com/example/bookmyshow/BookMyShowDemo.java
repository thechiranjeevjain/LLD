package com.example.bookmyshow;

import com.example.bookmyshow.domain.Booking;
import com.example.bookmyshow.domain.Money;
import com.example.bookmyshow.domain.Movie;
import com.example.bookmyshow.domain.Screen;
import com.example.bookmyshow.domain.Seat;
import com.example.bookmyshow.domain.SeatType;
import com.example.bookmyshow.domain.Theatre;
import com.example.bookmyshow.payment.AlwaysSuccessfulPaymentGateway;
import com.example.bookmyshow.repository.InMemoryBookingRepository;
import com.example.bookmyshow.repository.InMemoryShowRepository;
import com.example.bookmyshow.service.BookingService;
import com.example.bookmyshow.service.CatalogService;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;

public class BookMyShowDemo {

    public static void main(String[] args) {
        InMemoryShowRepository showRepository = new InMemoryShowRepository();
        InMemoryBookingRepository bookingRepository = new InMemoryBookingRepository();
        CatalogService catalogService = new CatalogService(showRepository);
        BookingService bookingService = new BookingService(
                showRepository,
                bookingRepository,
                new AlwaysSuccessfulPaymentGateway(),
                Clock.systemUTC(),
                Duration.ofMinutes(10)
        );

        Screen screen = new Screen("screen-1", "Audi 1", List.of(
                new Seat("A1", "A", 1, SeatType.REGULAR),
                new Seat("A2", "A", 2, SeatType.REGULAR),
                new Seat("B1", "B", 1, SeatType.PREMIUM)
        ));
        Movie movie = catalogService.registerMovie("Interstellar", "English", Duration.ofMinutes(169));
        Theatre theatre = catalogService.registerTheatre("PVR Orion", "Bengaluru", List.of(screen));
        catalogService.scheduleShow(movie, theatre, "screen-1", Instant.parse("2026-08-05T14:00:00Z"), Map.of(
                SeatType.REGULAR, Money.of("INR", 250),
                SeatType.PREMIUM, Money.of("INR", 450)
        ));

        Booking booking = bookingService.holdSeats("user-1", "show-1", List.of("A1", "A2"));
        Booking confirmed = bookingService.confirmBooking(booking.id());

        System.out.printf("Booking %s confirmed for seats %s. Amount: %s%n",
                confirmed.id(),
                confirmed.seatIds(),
                confirmed.amount()
        );
    }
}
