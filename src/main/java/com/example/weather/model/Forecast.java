package com.example.weather.model;

import java.util.List;
import java.util.Objects;

/**
 * A multi-day forecast for one city: an ordered list of {@link DailyForecast}
 * (typically five days).
 *
 * <p>The day list is defensively copied and made unmodifiable, so a
 * {@code Forecast} is fully immutable.
 */
public record Forecast(
        String city,
        String country,
        List<DailyForecast> days) {

    public Forecast {
        Objects.requireNonNull(city, "city");
        Objects.requireNonNull(country, "country");
        // List.copyOf rejects a null list or null elements and returns an
        // unmodifiable copy, so callers can't mutate our state afterwards.
        days = List.copyOf(days);
    }
}
