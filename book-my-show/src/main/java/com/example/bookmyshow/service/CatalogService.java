package com.example.bookmyshow.service;

import com.example.bookmyshow.domain.Money;
import com.example.bookmyshow.domain.Movie;
import com.example.bookmyshow.domain.Screen;
import com.example.bookmyshow.domain.SeatType;
import com.example.bookmyshow.domain.Show;
import com.example.bookmyshow.domain.Theatre;
import com.example.bookmyshow.repository.InMemoryShowRepository;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

public class CatalogService {
    private final InMemoryShowRepository showRepository;
    private final AtomicLong movieSequence = new AtomicLong();
    private final AtomicLong theatreSequence = new AtomicLong();
    private final AtomicLong showSequence = new AtomicLong();

    public CatalogService(InMemoryShowRepository showRepository) {
        this.showRepository = Objects.requireNonNull(showRepository, "showRepository");
    }

    public Movie registerMovie(String title, String language, Duration duration) {
        return new Movie("movie-" + movieSequence.incrementAndGet(), title, language, duration);
    }

    public Theatre registerTheatre(String name, String city, List<Screen> screens) {
        return new Theatre("theatre-" + theatreSequence.incrementAndGet(), name, city, screens);
    }

    public Show scheduleShow(
            Movie movie,
            Theatre theatre,
            String screenId,
            Instant startTime,
            Map<SeatType, Money> pricesBySeatType
    ) {
        Screen screen = theatre.screenById(screenId)
                .orElseThrow(() -> new IllegalArgumentException("screen not found: " + screenId));
        Show show = new Show(
                "show-" + showSequence.incrementAndGet(),
                movie,
                theatre,
                screen,
                startTime,
                pricesBySeatType
        );
        return showRepository.save(show);
    }

    public List<Show> searchShows(String city, String movieTitle, LocalDate date, ZoneId zoneId) {
        return showRepository.findByCityMovieAndDate(city, movieTitle, date, zoneId);
    }
}
