package com.example.weather.model;

import java.time.LocalDate;
import java.util.Objects;

/**
 * One day of forecast: the day's min/max temperature (°C) and the dominant
 * weather description for that day.
 */
public record DailyForecast(
        LocalDate date,
        double minTempC,
        double maxTempC,
        String description) {

    public DailyForecast {
        Objects.requireNonNull(date, "date");
        Objects.requireNonNull(description, "description");
        if (minTempC > maxTempC) {
            throw new IllegalArgumentException(
                    "minTempC (" + minTempC + ") must be <= maxTempC (" + maxTempC + ")");
        }
    }
}
