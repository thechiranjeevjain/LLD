package com.example.bookmyshow.repository;

import com.example.bookmyshow.domain.Show;
import com.example.bookmyshow.domain.ShowInventory;
import com.example.bookmyshow.exception.ShowNotFoundException;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class InMemoryShowRepository {
    private final ConcurrentMap<String, Show> showsById = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, ShowInventory> inventoriesByShowId = new ConcurrentHashMap<>();

    public Show save(Show show) {
        showsById.put(show.id(), show);
        inventoriesByShowId.putIfAbsent(show.id(), new ShowInventory(show));
        return show;
    }

    public Optional<Show> findById(String showId) {
        return Optional.ofNullable(showsById.get(showId));
    }

    public ShowInventory inventoryFor(String showId) {
        ShowInventory inventory = inventoriesByShowId.get(showId);
        if (inventory == null) {
            throw new ShowNotFoundException(showId);
        }
        return inventory;
    }

    public List<Show> findByCityMovieAndDate(String city, String movieTitle, LocalDate date, ZoneId zoneId) {
        String normalizedCity = normalize(city);
        String normalizedMovie = normalize(movieTitle);
        return showsById.values().stream()
                .filter(show -> normalize(show.theatre().city()).equals(normalizedCity))
                .filter(show -> normalize(show.movie().title()).equals(normalizedMovie))
                .filter(show -> LocalDate.ofInstant(show.startTime(), zoneId).equals(date))
                .sorted(Comparator.comparing(Show::startTime))
                .toList();
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }
}
