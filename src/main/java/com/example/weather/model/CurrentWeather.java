package com.example.weather.model;

import java.util.Objects;

/**
 * A current-weather snapshot for one city.
 *
 * <p>Temperatures are in degrees Celsius and wind speed in metres per second.
 */
public record CurrentWeather(
        String city,
        String country,
        double temperatureC,
        double feelsLikeC,
        int humidityPercent,
        double windSpeedMs,
        String description) {

    public CurrentWeather {
        Objects.requireNonNull(city, "city");
        Objects.requireNonNull(country, "country");
        Objects.requireNonNull(description, "description");
        if (humidityPercent < 0 || humidityPercent > 100) {
            throw new IllegalArgumentException(
                    "humidityPercent must be between 0 and 100, got " + humidityPercent);
        }
    }
}
