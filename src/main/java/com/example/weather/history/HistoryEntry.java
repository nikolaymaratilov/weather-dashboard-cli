package com.example.weather.history;

import com.example.weather.model.CurrentWeather;
import java.time.Instant;
import java.util.Objects;

/**
 * One recorded search: when it happened and the headline result.
 *
 * <p>Kept deliberately small — just enough to show a useful history list —
 * rather than storing the full weather payload.
 */
public record HistoryEntry(
        Instant timestamp,
        String city,
        String country,
        double temperatureC,
        String description) {

    public HistoryEntry {
        Objects.requireNonNull(timestamp, "timestamp");
        Objects.requireNonNull(city, "city");
        Objects.requireNonNull(country, "country");
        Objects.requireNonNull(description, "description");
    }

    /** Builds a history entry from a current-weather result at the given time. */
    public static HistoryEntry of(Instant when, CurrentWeather weather) {
        return new HistoryEntry(
                when,
                weather.city(),
                weather.country(),
                weather.temperatureC(),
                weather.description());
    }
}
