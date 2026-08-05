package com.example.bookmyshow.service;

import com.example.bookmyshow.domain.Booking;
import com.example.bookmyshow.domain.BookingStatus;
import com.example.bookmyshow.domain.Money;
import com.example.bookmyshow.domain.Movie;
import com.example.bookmyshow.domain.Screen;
import com.example.bookmyshow.domain.Seat;
import com.example.bookmyshow.domain.SeatType;
import com.example.bookmyshow.domain.Show;
import com.example.bookmyshow.domain.Theatre;
import com.example.bookmyshow.exception.SeatUnavailableException;
import com.example.bookmyshow.payment.AlwaysSuccessfulPaymentGateway;
import com.example.bookmyshow.repository.InMemoryBookingRepository;
import com.example.bookmyshow.repository.InMemoryShowRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BookingServiceTest {
    private static final ZoneId ZONE = ZoneId.of("Asia/Kolkata");

    private MutableClock clock;
    private InMemoryShowRepository showRepository;
    private BookingService bookingService;
    private CatalogService catalogService;

    @BeforeEach
    void setUp() {
        clock = new MutableClock(Instant.parse("2026-08-05T10:00:00Z"), ZONE);
        showRepository = new InMemoryShowRepository();
        InMemoryBookingRepository bookingRepository = new InMemoryBookingRepository();
        catalogService = new CatalogService(showRepository);
        bookingService = new BookingService(
                showRepository,
                bookingRepository,
                new AlwaysSuccessfulPaymentGateway(),
                clock,
                Duration.ofMinutes(5)
        );
        createDefaultShow();
    }

    @Test
    void holdsThenConfirmsSeats() {
        Booking booking = bookingService.holdSeats("user-1", "show-1", List.of("A1", "A2"));

        assertEquals(BookingStatus.PENDING, booking.status());
        assertEquals(List.of("B1"), bookingService.availableSeats("show-1").stream().map(Seat::id).toList());

        Booking confirmed = bookingService.confirmBooking(booking.id());

        assertEquals(BookingStatus.CONFIRMED, confirmed.status());
        assertEquals("payment-1", confirmed.paymentId());
        assertEquals(List.of("B1"), bookingService.availableSeats("show-1").stream().map(Seat::id).toList());
    }

    @Test
    void rejectsSeatsHeldByAnotherUser() {
        bookingService.holdSeats("user-1", "show-1", List.of("A1"));

        SeatUnavailableException exception = assertThrows(
                SeatUnavailableException.class,
                () -> bookingService.holdSeats("user-2", "show-1", List.of("A1"))
        );

        assertEquals(List.of("A1"), exception.seatIds());
    }

    @Test
    void releasesExpiredHolds() {
        Booking booking = bookingService.holdSeats("user-1", "show-1", List.of("A1"));

        clock.advance(Duration.ofMinutes(6));
        Booking replacement = bookingService.holdSeats("user-2", "show-1", List.of("A1"));

        assertEquals(BookingStatus.EXPIRED, booking.status());
        assertEquals(BookingStatus.PENDING, replacement.status());
    }

    @Test
    void cancelConfirmedBookingReleasesSeats() {
        Booking booking = bookingService.holdSeats("user-1", "show-1", List.of("A1"));
        bookingService.confirmBooking(booking.id());

        bookingService.cancelBooking(booking.id());

        assertEquals(BookingStatus.CANCELLED, booking.status());
        assertTrue(bookingService.availableSeats("show-1").stream().map(Seat::id).toList().contains("A1"));
    }

    @Test
    void onlyOneConcurrentBookingCanConfirmSameSeat() throws Exception {
        int workers = 10;
        CountDownLatch start = new CountDownLatch(1);
        var executor = Executors.newFixedThreadPool(workers);
        try {
            List<Callable<Boolean>> tasks = java.util.stream.IntStream.range(0, workers)
                    .mapToObj(index -> (Callable<Boolean>) () -> {
                        start.await();
                        try {
                            Booking booking = bookingService.holdSeats("user-" + index, "show-1", List.of("A1"));
                            bookingService.confirmBooking(booking.id());
                            return true;
                        } catch (SeatUnavailableException exception) {
                            return false;
                        }
                    })
                    .toList();

            var futures = tasks.stream().map(executor::submit).toList();
            start.countDown();

            long confirmed = 0;
            for (var future : futures) {
                if (future.get(5, TimeUnit.SECONDS)) {
                    confirmed++;
                }
            }

            assertEquals(1, confirmed);
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    void searchesShowsByCityMovieAndDate() {
        List<Show> shows = catalogService.searchShows(
                "bengaluru",
                "interstellar",
                LocalDate.of(2026, 8, 5),
                ZONE
        );

        assertEquals(1, shows.size());
        assertEquals("show-1", shows.get(0).id());
    }

    private void createDefaultShow() {
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
    }

    private static final class MutableClock extends Clock {
        private Instant instant;
        private final ZoneId zone;

        private MutableClock(Instant instant, ZoneId zone) {
            this.instant = instant;
            this.zone = zone;
        }

        @Override
        public ZoneId getZone() {
            return zone;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return new MutableClock(instant, zone);
        }

        @Override
        public Instant instant() {
            return instant;
        }

        private void advance(Duration duration) {
            instant = instant.plus(duration);
        }
    }
}
